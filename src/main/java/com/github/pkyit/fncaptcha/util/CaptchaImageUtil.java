package com.github.pkyit.fncaptcha.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 滑块验证码图片生成工具类（圆形缺口版，固定 300×150 画布）
 */
public class CaptchaImageUtil {

	private static final String IMAGE_DIR = "bg_images/"; // 图片目录
	private static final int IMAGE_COUNT = 40; // 图片数量
	private static final List<String> IMAGE_NAMES = new ArrayList<>(); //  图片名称列表

	static {
		// 读取图片名称列表
		for (int i = 1; i <= IMAGE_COUNT; i++) {
			IMAGE_NAMES.add(i + ".png");
		}
	}

	@Data
	public static class CaptchaImageResult {
		private String backgroundBase64;  // data:image/jpeg;base64,...
		private String sliderBase64;      // data:image/png;base64,...
		private int gapX;
		private int gapY;
		private int blockSize = 36;       // 固定滑块/圆直径 36px
	}

	/**
	 * 生成圆形缺口验证码图片
	 * 画布：300×150
	 * 滑块：36×36 圆形
	 * 缺口位置随机：x 60~210, y 40~60
	 */
	public static CaptchaImageResult generate() throws IOException {
		// 随机选一张图片
		String imageName = RandomUtil.randomEle(IMAGE_NAMES);
		InputStream is = CaptchaImageUtil.class.getClassLoader().getResourceAsStream(IMAGE_DIR + imageName);
		if (is == null) {
			throw new IOException("图片不存在: " + IMAGE_DIR + imageName);
		}

		BufferedImage original = ImageIO.read(is);
		IoUtil.close(is);

		// 随机缺口中心位置（圆心）
		int centerX = RandomUtil.randomInt(40 + 18, 230 + 18); // 圆心 x 保证圆不超出左右
		int centerY = RandomUtil.randomInt(30 + 18, 100 + 18);  // 圆心 y 保证圆在上半部分居中

		// 缺口左上角（用于返回坐标）
		int gapX = centerX - 18;
		int gapY = centerY - 18;

		// 生成背景图（带圆形缺口）
		BufferedImage background = createBackgroundWithCircleGap(original, centerX, centerY,true);

		// 生成圆形滑块（透明背景）
		BufferedImage slider = createCircleSlider(original, centerX, centerY);

		// 转 base64
		String bgBase64 = imageToBase64(background, "jpg");
		String sliderBase64 = imageToBase64(slider, "png");

		CaptchaImageResult result = new CaptchaImageResult();
		result.setBackgroundBase64("data:image/jpeg;base64," + bgBase64);
		result.setSliderBase64("data:image/png;base64," + sliderBase64);
		result.setGapX(gapX);
		result.setGapY(gapY);

		return result;
	}

	/**
	 * 创建带圆形缺口的背景图
	 *
	 * @param centerX 圆心 x
	 * @param centerY 圆心 y
	 */
	private static BufferedImage createBackgroundWithCircleGap(BufferedImage source, int centerX, int centerY,boolean noiseEnabled) {
		// 使用 RGB 类型，不带透明通道
		BufferedImage bg = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bg.createGraphics();
		g.drawImage(source, 0, 0, null);

		// 使用半透明灰色填充圆形区域
		g.setColor(new Color(144, 143, 143, 200));
		g.fillOval(centerX - 18, centerY - 18, 18 * 2, 18 * 2);

		// 绘制干扰区域
		if (noiseEnabled) {
			int noiseX = 18;
			int noiseY = 18;
			switch (RandomUtil.randomInt(1, 5)) {
				case 1:
					noiseX = RandomUtil.randomInt(19, 58);
					noiseY = RandomUtil.randomInt(19, 48);
					break;
				case 2:
					noiseX = RandomUtil.randomInt(19, 58);
					noiseY = RandomUtil.randomInt(119, 132);
					break;
				case 3:
					noiseX = RandomUtil.randomInt(248, 282);
					noiseY = RandomUtil.randomInt(19, 48);
					break;
				case 4:
					noiseX = RandomUtil.randomInt(248, 282);
					noiseY = RandomUtil.randomInt(119, 132);
					break;
			}
			g.setColor(new Color(143, 143, 143, 200));
			g.fillOval(noiseX - 18, noiseY - 18, 18 * 2, 18 * 2);
		}
		g.dispose();
		return bg;
	}

	/**
	 * 创建圆形滑块（透明背景）
	 */
	private static BufferedImage createCircleSlider(BufferedImage source, int centerX, int centerY) {
		int diameter = 18 * 2;
		// 创建一个透明的缓冲图像，大小为直径×直径
		BufferedImage slider = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = slider.createGraphics();

		// 设置抗锯齿等渲染属性（可选）
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 剪裁圆形区域
		g.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));

		// 在新图像的正确位置绘制原始图像的一部分
		g.drawImage(source, -centerX + 18, -centerY + 18, null);

		// 加白色半透明圆边框（视觉提示，可选去掉）
		g.setColor(new Color(255, 255, 255, 100));
		g.setStroke(new BasicStroke(2));
		g.drawOval(0, 0, diameter - 1, diameter - 1);

		g.dispose();
		return slider;
	}
	private static String imageToBase64(BufferedImage image, String format) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, format, baos);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
	}

	// 测试用
	public static void main(String[] args) throws Exception {
		CaptchaImageResult result = generate();

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
