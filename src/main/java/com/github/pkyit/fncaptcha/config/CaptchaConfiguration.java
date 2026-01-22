package com.github.pkyit.fncaptcha.config;

import com.github.pkyit.fncaptcha.domain.consts.CaptchaImageConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties({CaptchaConfigProperties.class})
public class CaptchaConfiguration {

	private final CaptchaConfigProperties captchaConfigProperties;

	public CaptchaConfiguration(CaptchaConfigProperties captchaConfigProperties) {
		this.captchaConfigProperties = captchaConfigProperties;
	}

	/**
	 * 预加载验证码图片到内存中
	 */
	@PostConstruct
	public void preloadCaptchaImages() {
		log.info("开始预加载验证码图片...");

		// 根据配置动态设置常量值
		CaptchaImageConst.setIMAGE_DIR(captchaConfigProperties.getImagePath());
		CaptchaImageConst.setIMAGE_COUNT(captchaConfigProperties.getImageCount());
		CaptchaImageConst.setIMAGE_TYPE(captchaConfigProperties.getImageType());

		// 初始化图片名称列表
		List<String> imageNames = new ArrayList<>();
		for (int i = 1; i <= captchaConfigProperties.getImageCount(); i++) {
			String imageName = i + captchaConfigProperties.getImageType();
			// 验证图片是否存在
			Resource resource = new ClassPathResource(captchaConfigProperties.getImagePath() + imageName);
			try {
				if (resource.exists()) {
					imageNames.add(imageName);
				} else {
					log.warn("验证码图片不存在: {}{}", captchaConfigProperties.getImagePath(), imageName);
				}
			} catch (Exception e) {
				log.error("检查验证码图片时发生错误: {}{}", captchaConfigProperties.getImagePath(), imageName, e);
			}
		}

		CaptchaImageConst.setIMAGE_NAMES(imageNames);
		log.info("验证码图片预加载完成，共加载 {} 张图片", imageNames.size());
	}

	/**
	 * 提供预加载的图片名称列表作为Bean
	 */
	@Bean
	public List<String> captchaImageNames() {
		return CaptchaImageConst.getIMAGE_NAMES();
	}
}
