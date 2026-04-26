package com.github.pkyit.fncaptcha.service.impl;

import cn.hutool.core.util.IdUtil;
import com.github.pkyit.fncaptcha.component.TrajectoryAnalyzer;
import com.github.pkyit.fncaptcha.component.TrajectoryAnalysisResult;
import com.github.pkyit.fncaptcha.config.CaptchaConfigProperties;
import com.github.pkyit.fncaptcha.config.CaptchaConfiguration;
import com.github.pkyit.fncaptcha.domain.bo.CaptchaImageBO;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaGenerateResponse;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaVerifyDTO;
import com.github.pkyit.fncaptcha.domain.dto.Result;
import com.github.pkyit.fncaptcha.domain.entity.CaptchaImageRepository;
import com.github.pkyit.fncaptcha.service.SliderCaptchaService;
import com.github.pkyit.fncaptcha.util.CaptchaImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 滑块验证码服务实现
 * <p>生成验证码时将缺口位置存入 Redis，返回给客户端不包含 X 坐标。
 * 验证时校验滑动距离偏差，同时通过轨迹分析判断是否为人类操作。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SliderCaptchaServiceImpl implements SliderCaptchaService {

    /** Redis 键前缀（格式：fncaptcha:captcha:{captchaId}） */
    private static final String CAPTCHA_KEY_PREFIX = "fncaptcha:captcha:";

    /** 滑动距离误差容限（像素），允许 ±3px 偏差 */
    private static final int VERIFY_TOLERANCE = 3;

    private final CaptchaConfiguration captchaConfiguration;
    private final CaptchaConfigProperties captchaConfigProperties;
    private final RedissonClient redissonClient;
    private final TrajectoryAnalyzer trajectoryAnalyzer;

    /**
     * 生成滑块验证码
     * <p>随机选取背景图 -> 生成圆形缺口和滑块 -> 存入 Redis（含 gapX）-> 返回（不含 gapX）。</p>
     *
     * @param clientId 客户端唯一标识
     * @return 验证码数据（captchaId、背景图 base64、滑块图 base64、滑块 Y 轴位置）
     * @throws Exception 图片读写或编码异常
     */
    @Override
    public CaptchaGenerateResponse generate(String clientId) throws Exception {
        File imageFile = captchaConfiguration.getRandomImageFile();
        CaptchaImageBO bo = CaptchaImageUtils.generate(imageFile, captchaConfigProperties.isDifficult());

        String captchaId = IdUtil.fastSimpleUUID();

        // 将验证码完整数据（含 gapX）存入 Redis，设置过期时间
        CaptchaImageRepository repo = new CaptchaImageRepository(
                captchaId,
                bo.getBackgroundBase64(),
                bo.getSliderBase64(),
                bo.getGapX(),
                bo.getGapY(),
                clientId,
                captchaConfigProperties.getExpireTime()
        );

        RBucket<CaptchaImageRepository> bucket = redissonClient.getBucket(CAPTCHA_KEY_PREFIX + captchaId);
        bucket.set(repo, captchaConfigProperties.getExpireTime(), TimeUnit.SECONDS);

        log.debug("验证码已生成，clientId={}, captchaId={}, gapX={}, gapY={}", clientId, captchaId, bo.getGapX(), bo.getGapY());

        // 返回给前端时不暴露 gapX，防止前端直接读取缺口位置
        return CaptchaGenerateResponse.builder()
                .captchaId(captchaId)
                .backgroundBase64(bo.getBackgroundBase64())
                .sliderBase64(bo.getSliderBase64())
                .gapY(bo.getGapY())
                .build();
    }

    /**
     * 校验验证码
     * <p>验证流程：</p>
     * <ol>
     *   <li>从 Redis 获取验证码数据，不存在则返回过期</li>
     *   <li>立即删除 Redis 数据，确保一次性使用</li>
     *   <li>先做轨迹行为分析，判断是否为人类操作</li>
     *   <li>再校验滑动距离是否在容差范围内</li>
     * </ol>
     *
     * @param dto 验证请求参数
     * @return 验证结果
     */
    @Override
    public Result<Object> verify(CaptchaVerifyDTO dto) {
        RBucket<CaptchaImageRepository> bucket = redissonClient.getBucket(CAPTCHA_KEY_PREFIX + dto.getCaptchaId());
        CaptchaImageRepository repo = bucket.get();

        if (repo == null) {
            return Result.fail("验证码已过期或不存在");
        }

        // 一次性使用：无论验证结果如何，删除 Redis 中的验证码数据
        bucket.delete();

        // 第一步：轨迹行为分析（抗自动化脚本）
        TrajectoryAnalysisResult trajectoryResult = trajectoryAnalyzer.analyze(dto.getTrajectory());
        if (!trajectoryResult.isPassed()) {
            log.debug("轨迹分析未通过，id={}, reason={}", dto.getCaptchaId(), trajectoryResult.getReason());
            return Result.fail(trajectoryResult.getReason());
        }

        // 第二步：校验滑块位置偏差
        int distance = repo.getGapX() - dto.getSliderX();

        if (Math.abs(distance) <= VERIFY_TOLERANCE) {
            log.debug("验证码校验通过，id={}, sliderX={}, expected gapX={}, distance={}", dto.getCaptchaId(), dto.getSliderX(), repo.getGapX(), distance);
            return Result.ok("验证通过");
        }

        log.debug("验证码校验失败，id={}, sliderX={}, expected gapX={}, distance={}", dto.getCaptchaId(), dto.getSliderX(), repo.getGapX(), distance);
        return Result.fail("验证失败，请重试");
    }
}
