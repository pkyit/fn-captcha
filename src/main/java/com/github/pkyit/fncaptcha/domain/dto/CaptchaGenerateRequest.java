package com.github.pkyit.fncaptcha.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码生成请求参数
 * <p>前端调用生成接口时传入客户端标识。</p>
 */
@Data
public class CaptchaGenerateRequest {

    /** 客户端唯一标识，用于区分不同的用户会话 */
    @NotBlank(message = "clientId不能为空")
    private String clientId;
}
