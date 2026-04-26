package com.github.pkyit.fncaptcha.controller;

import com.github.pkyit.fncaptcha.domain.dto.CaptchaGenerateRequest;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaGenerateResponse;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaVerifyDTO;
import com.github.pkyit.fncaptcha.domain.dto.Result;
import com.github.pkyit.fncaptcha.service.SliderCaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 滑块验证码接口控制器
 * <p>提供验证码的生成与验证两个 REST 接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final SliderCaptchaService sliderCaptchaService;

    /**
     * 生成滑块验证码
     * <p>根据客户端标识生成滑块验证码图片，返回背景图、滑块图 base64 及滑块 Y 轴位置。</p>
     *
     * @param request 生成请求，包含客户端标识 clientId
     * @return 验证码图片数据（不含缺口 X 坐标，防止前端作弊）
     */
    @PostMapping("/generate")
    public Result<CaptchaGenerateResponse> generate(@Valid @RequestBody CaptchaGenerateRequest request) {
        try {
            CaptchaGenerateResponse result = sliderCaptchaService.generate(request.getClientId());
            return Result.<CaptchaGenerateResponse>ok("生成成功", result);
        } catch (Exception e) {
            log.error("验证码生成失败", e);
            return Result.<CaptchaGenerateResponse>fail("验证码生成失败，请重试");
        }
    }

    /**
     * 验证滑块验证码
     * <p>校验用户滑动距离与缺口位置的偏差，同时对滑动轨迹进行行为分析，防止自动化脚本绕过。</p>
     *
     * @param dto 验证请求，包含验证码ID、客户端标识、滑块X坐标及轨迹数据
     * @return 验证结果（成功/失败及原因）
     */
    @PostMapping("/verify")
    public Result<Object> verify(@Valid @RequestBody CaptchaVerifyDTO dto) {
        return sliderCaptchaService.verify(dto);
    }
}
