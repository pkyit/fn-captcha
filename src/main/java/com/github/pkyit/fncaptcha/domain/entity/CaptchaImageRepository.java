package com.github.pkyit.fncaptcha.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动滑块验证码的存储类
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class CaptchaImageRepository implements java.io.Serializable {
	private static final long serialVersionUID = 1329483329489434L;

	/**
	 * 验证码id
	 *
	 */
	private String id;

	/**
	 * 验证码背景图
	 *
	 */
	private String backgroundBase64;

	/**
	 * 验证码滑块
	 *
	 */
	private String sliderBase64;

	/**
	 * 验证码缺口X轴位置
	 *
	 */
	private int gapX;

	/**
	 * 验证码缺口Y轴位置
	 *
	 */
	private int gapY;

	/**
	 * 验证码密钥
	 *
	 */
	private String cryptoToken;

}
