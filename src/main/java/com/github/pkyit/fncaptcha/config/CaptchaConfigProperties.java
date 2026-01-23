package com.github.pkyit.fncaptcha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = CaptchaConfigProperties.CAPTCHA_CONFIG_PROPERTIES_PREFIX)
public class CaptchaConfigProperties {
	protected static final String CAPTCHA_CONFIG_PROPERTIES_PREFIX = "pkyit.fncaptch.config";

	/**
	 * 是否开启参数加密，默认关闭
	 */
	private boolean encryption = false;

	/**
	 * 是否开启困难模式，默认关闭
	 */
	private boolean difficult = false;

	/**
	 * 验证码有效时间，默认120秒
	 */
	private int expireTime = 120;

	/**
	 * 滑动验证码背景图片路径，如果不指定默认为resources下的bg_images目录
	 */
	private String imagePath = "classpath:bg_images/";

	/**
	 * 滑动验证码背景图片数量
	 */
	private int imageCount = 40;

	/**
	 * 滑动验证码图片格式
	 */
	private String imageType = ".png";

}
