package com.github.pkyit.fncaptcha.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码生成响应数据
 * <p>返回给前端的数据不包含缺口 X 坐标（gapX），
 * 防止前端直接读取缺口位置作弊。</p>
 */
@Data
@Builder
public class CaptchaGenerateResponse implements java.io.Serializable {
    private static final long serialVersionUID = 42294833294895L;

    /** 验证码唯一标识 */
    private String captchaId;

    /** 背景图 base64 编码（JPEG 格式，含 data:image/jpeg;base64, 前缀） */
    private String backgroundBase64;

    /** 滑块图 base64 编码（PNG 格式，透明背景，含 data:image/png;base64, 前缀） */
    private String sliderBase64;

    /** 滑块 Y 轴起始位置（用于前端定位滑块初始高度） */
    private int gapY;
}
