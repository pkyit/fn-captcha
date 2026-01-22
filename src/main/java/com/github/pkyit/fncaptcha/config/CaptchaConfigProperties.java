package com.github.pkyit.fncaptcha.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@ConfigurationProperties(prefix = "pkyit.fncaptch.config")
public class CaptchaConfigProperties {

//	private


	/**
	 * 滑动验证码背景图片路径
	 */
	private String imagePath = "bg_images/";

	/**
	 * 华东验证码背景图片数量
	 */
	private int imageCount = 40;

	/**
	 * 滑动验证码图片格式
	 */
	private String imageType = ".png";

	/**
	 * 验证码图片名称列表
	 */
	private List<String> imageNames = new ArrayList<>();

	@PostConstruct
	private void init() {
		if (this.imageNames.isEmpty()) {
			this.imageNames = generateDefaultImageNames(this.imagePath, this.imageCount, this.imageType);
		}
	}

	private static List<String> generateDefaultImageNames(String path, int count, String type) {
		return IntStream.rangeClosed(1, count)
			.mapToObj(i -> "classpath:" + (path != null ? path : "bg_images/") + i + (type != null ? type : ".png"))
			.collect(Collectors.toList());
	}
}
