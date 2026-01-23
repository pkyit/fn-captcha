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
	 * 检查验证码图片有效性
	 * */
	@PostConstruct
	public void checkImageValidity() {
		List<String> imageNames = new ArrayList<>();

	}
}
