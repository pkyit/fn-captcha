package com.github.pkyit.fncaptcha.domain.consts;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class CaptchaImageConst {

	@Getter
	@Setter
	public static String IMAGE_DIR = "bg_images/"; // 图片目录

	@Getter
	@Setter
	public static int IMAGE_COUNT = 40; // 图片数量

	@Getter
	@Setter
	public static String IMAGE_TYPE = ".png";

	@Getter
	@Setter
	public static List<String> IMAGE_NAMES; //  图片名称列表

	/**
	 * 获取验证码缓存初始化的分布式锁key
	 *
	 */
	public static final String CACHE_INIT_LOCK_KEY = "fncaptcha:lock:cache:warmup";


	// 静态初始化验证码底图
	static {
		IMAGE_NAMES = new ArrayList<>(IMAGE_COUNT);
		// 读取图片名称列表
		for (int i = 1; i <= IMAGE_COUNT; i++) {
			IMAGE_NAMES.add(i + IMAGE_TYPE);
		}
	}

}