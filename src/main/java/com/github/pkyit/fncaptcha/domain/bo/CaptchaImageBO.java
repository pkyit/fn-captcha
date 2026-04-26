package com.github.pkyit.fncaptcha.domain.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码图片业务对象
 * <p>封装 {@code CaptchaImageUtils.generate()} 的返回结果，
 * 包含背景图、滑块图 base64 以及缺口坐标，供 Service 层使用。</p>
 */
@Data
@Builder
public class CaptchaImageBO implements java.io.Serializable {
    private static final long serialVersionUID = 333294833294844L;

    /** 带缺口的背景图 base64 */
    private String backgroundBase64;

    /** 圆形滑块图 base64 */
    private String sliderBase64;

    /** 缺口 X 轴位置（滑块目标位置） */
    private int gapX;

    /** 缺口 Y 轴位置 */
    private int gapY;
}
