package com.github.pkyit.fncaptcha;

import com.github.pkyit.fncaptcha.util.CaptchaImageUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@SpringBootTest
class FnCaptchaApplicationTests {

    @Test
	void testGenerateCaptcha() throws IOException {
		com.github.pkyit.fncaptcha.domain.dto.CaptchaImageResultDTO result = CaptchaImageUtils.generate();

		System.out.println("GapX: " + result.getGapX() + ", GapY: " + result.getGapY());
		System.out.println("Bg length: " + result.getBackgroundBase64().length());
		System.out.println("Slider length: " + result.getSliderBase64().length());

		// 保存背景图（去掉前缀 "data:image/jpeg;base64,"）
		String bgBase64Clean = result.getBackgroundBase64().replace("data:image/jpeg;base64,", "");
		byte[] bgBytes = Base64.getDecoder().decode(bgBase64Clean);
		Files.write(Paths.get("background.jpg"), bgBytes);
		System.out.println("已保存背景图到: background.jpg");

		// 保存滑块图（去掉前缀 "data:image/png;base64,"）
		String sliderBase64Clean = result.getSliderBase64().replace("data:image/png;base64,", "");
		byte[] sliderBytes = Base64.getDecoder().decode(sliderBase64Clean);
		Files.write(Paths.get("slider.png"), sliderBytes);
		System.out.println("已保存滑块图到: slider.png");
	}
}
