package com.github.pkyit.fncaptcha.component;

import com.github.pkyit.fncaptcha.domain.consts.CaptchaImageConst;
import com.github.pkyit.fncaptcha.util.CaptchaImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitCaptchaCacheRunner implements ApplicationRunner {

	private final RedissonClient redissonClient;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("开始初始化验证码缓存...");

		// 获取分布式锁，防止多实例重复初始化
		RLock lock = redissonClient.getLock(CaptchaImageConst.CACHE_INIT_LOCK_KEY);

		try {
			if (lock.tryLock()) {
				log.info("获取到分布式锁，开始初始化验证码缓存");

				// 验证所有配置的图片是否存在
				List<String> imageNames = CaptchaImageConst.getIMAGE_NAMES();
				String imagePath = CaptchaImageConst.getIMAGE_DIR();

				log.info("验证 {} 个验证码图片...", imageNames.size());
				for (String imageName : imageNames) {
					ClassPathResource resource = new ClassPathResource(imagePath + imageName);
					if (!resource.exists()) {
						log.warn("验证码图片不存在: {}{}", imagePath, imageName);
					}
				}

				log.info("验证码缓存初始化完成，共验证 {} 张图片", imageNames.size());
			} else {
				log.info("未获取到分布式锁，其他实例可能正在初始化验证码缓存");
			}
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}
}
