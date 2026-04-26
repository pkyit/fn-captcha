package com.github.pkyit.fncaptcha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 滑块验证码配置属性
 * <p>绑定配置文件前缀 {@code pkyit.fncaptch.config} 下的属性，
 * 包含加密开关、困难模式、过期时间、背景图片路径等。</p>
 */
@Data
@ConfigurationProperties(prefix = CaptchaConfigProperties.CAPTCHA_CONFIG_PROPERTIES_PREFIX)
public class CaptchaConfigProperties {
    protected static final String CAPTCHA_CONFIG_PROPERTIES_PREFIX = "pkyit.fncaptch.config";

    /** 是否开启参数加密，默认关闭 */
    private boolean encryption = false;

    /** 是否开启困难模式（增加干扰圆），默认关闭 */
    private boolean difficult = false;

    /** 验证码有效时间（秒），默认120秒 */
    private int expireTime = 120;

    /** 滑动验证码背景图片路径，不指定默认为 resources 下 bg_images 目录 */
    private String imagePath = "classpath:bg_images/";

    /** 滑动验证码背景图片数量 */
    private int imageCount = 40;
}
