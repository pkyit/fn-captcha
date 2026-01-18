package com.github.pkyit.fncaptcha.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaImageResultDTO implements java.io.Serializable {
	private static final long serialVersionUID = 13294833294894L;

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
}
