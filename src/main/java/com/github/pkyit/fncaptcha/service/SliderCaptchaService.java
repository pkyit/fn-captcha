package com.github.pkyit.fncaptcha.service;

import com.github.pkyit.fncaptcha.domain.dto.CaptchaGenerateResponse;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaVerifyDTO;
import com.github.pkyit.fncaptcha.domain.dto.Result;

/**
 * 滑块验证码业务接口
 * <p>定义验证码的生成与验证两大核心业务。</p>
 */
public interface SliderCaptchaService {

    /**
     * 生成滑块验证码
     *
     * @param clientId 客户端唯一标识
     * @return 验证码响应数据（背景图、滑块图 base64、滑块 Y 轴位置）
     * @throws Exception 图片处理异常
     */
    CaptchaGenerateResponse generate(String clientId) throws Exception;

    /**
     * 校验验证码结果
     * <p>检查滑动距离偏差 + 用户滑动轨迹行为分析。</p>
     *
     * @param dto 验证请求参数
     * @return 验证结果
     */
    Result<Object> verify(CaptchaVerifyDTO dto);
}
