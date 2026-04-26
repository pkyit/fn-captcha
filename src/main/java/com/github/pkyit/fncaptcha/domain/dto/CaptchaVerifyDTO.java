package com.github.pkyit.fncaptcha.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 验证码校验请求参数
 * <p>前端提交滑动结果时传入验证码ID、客户端标识、
 * 滑块最终 X 坐标及完整的鼠标轨迹数据用于行为分析。</p>
 */
@Data
public class CaptchaVerifyDTO implements Serializable {
    private static final long serialVersionUID = 52294833294894L;

    /** 验证码唯一标识（生成接口返回） */
    @NotBlank(message = "captchaId不能为空")
    private String captchaId;

    /** 客户端唯一标识 */
    @NotBlank(message = "clientId不能为空")
    private String clientId;

    /** 滑块最终停留的 X 坐标 */
    @NotNull(message = "sliderX不能为空")
    private Integer sliderX;

    /** 滑动轨迹点集合（用于行为分析防刷） */
    @NotEmpty(message = "轨迹数据不能为空")
    private List<TrajectoryPoint> trajectory;
}
