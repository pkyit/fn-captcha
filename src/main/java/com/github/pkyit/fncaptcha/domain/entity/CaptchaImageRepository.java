package com.github.pkyit.fncaptcha.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 验证码数据存储实体
 * <p>此对象序列化后存入 Redis，key 格式为 {@code fncaptcha:captcha:{captchaId}}，
 * 包含生成验证码时所有的图片数据和缺口位置。</p>
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaImageRepository implements java.io.Serializable {
    private static final long serialVersionUID = 1329483329489434L;

    /** 验证码唯一标识 */
    private String id;

    /** 背景图 base64 */
    private String backgroundBase64;

    /** 滑块图 base64 */
    private String sliderBase64;

    /** 缺口 X 轴位置（仅服务端校验时使用，不返回给前端） */
    private int gapX;

    /** 缺口 Y 轴位置（返回给前端定位滑块） */
    private int gapY;

    /** 客户端唯一标识 */
    private String clientId;

    /** 过期时间（秒） */
    private Integer expireTime;
}
