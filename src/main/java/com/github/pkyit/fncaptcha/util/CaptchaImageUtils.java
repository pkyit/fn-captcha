package com.github.pkyit.fncaptcha.util;

import cn.hutool.core.util.RandomUtil;
import com.github.pkyit.fncaptcha.domain.bo.CaptchaImageBO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * 滑块验证码图片生成工具类
 * <p>在 300×150 的背景图上随机位置挖出 36×36 的圆形缺口，并生成对应的圆形滑块图。
 * 支持困难模式（在角落添加干扰圆）。</p>
 */
public class CaptchaImageUtils {

    /**
     * 生成滑块验证码图片
     *
     * @param imageFile    背景图片文件
     * @param noiseEnabled 是否开启干扰模式（困难模式下添加干扰圆）
     * @return 包含背景图、滑块图 base64 及缺口坐标的业务对象
     * @throws IOException 图片读取或编码异常
     */
    public static CaptchaImageBO generate(File imageFile, boolean noiseEnabled) throws IOException {
        BufferedImage original = ImageIO.read(imageFile);

        // 缺口圆心坐标：x 范围 58~248，y 范围 48~118（确保 36px 直径圆不超出 300×150 画布边界）
        int centerX = RandomUtil.randomInt(40 + 18, 230 + 18);
        int centerY = RandomUtil.randomInt(30 + 18, 100 + 18);

        int gapX = centerX - 18;
        int gapY = centerY - 18;

        BufferedImage background = createBackgroundWithCircleGap(original, centerX, centerY, noiseEnabled);
        BufferedImage slider = createCircleSlider(original, centerX, centerY);

        String bgBase64 = imageToBase64(background, "jpg");
        String sliderBase64 = imageToBase64(slider, "png");

        return CaptchaImageBO.builder()
                .backgroundBase64("data:image/jpeg;base64," + bgBase64)
                .sliderBase64("data:image/png;base64," + sliderBase64)
                .gapX(gapX)
                .gapY(gapY)
                .build();
    }

    /**
     * 创建带圆形缺口的背景图
     * <p>在圆心位置以半透明灰色填充圆形区域，模拟挖空效果。
     * 困难模式下在角落随机添加一个同样大小的干扰圆。</p>
     *
     * @param source       原始背景图
     * @param centerX      缺口圆心 X
     * @param centerY      缺口圆心 Y
     * @param noiseEnabled 是否添加干扰圆
     * @return 处理后的背景图
     */
    private static BufferedImage createBackgroundWithCircleGap(BufferedImage source, int centerX, int centerY, boolean noiseEnabled) {
        BufferedImage bg = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bg.createGraphics();
        g.drawImage(source, 0, 0, null);

        // 半透明灰色填充缺口区域
        g.setColor(new Color(144, 143, 143, 200));
        g.fillOval(centerX - 18, centerY - 18, 18 * 2, 18 * 2);

        if (noiseEnabled) {
            // 在画布四角之一随机位置画干扰圆，诱导非人类操作
            int noiseX = 18;
            int noiseY = switch (RandomUtil.randomInt(1, 5)) {
                case 1 -> {
                    noiseX = RandomUtil.randomInt(19, 58);
                    yield RandomUtil.randomInt(19, 48);
                }
                case 2 -> {
                    noiseX = RandomUtil.randomInt(19, 58);
                    yield RandomUtil.randomInt(119, 132);
                }
                case 3 -> {
                    noiseX = RandomUtil.randomInt(248, 282);
                    yield RandomUtil.randomInt(19, 48);
                }
                case 4 -> {
                    noiseX = RandomUtil.randomInt(248, 282);
                    yield RandomUtil.randomInt(119, 132);
                }
                default -> 18;
            };
            g.setColor(new Color(143, 143, 143, 205));
            g.fillOval(noiseX - 18, noiseY - 18, 18 * 2, 18 * 2);
        }
        g.dispose();
        return bg;
    }

    /**
     * 创建圆形滑块图（透明背景）
     * <p>从原图上裁剪出缺口位置的圆形区域，添加白色半透明描边便于视觉识别。</p>
     *
     * @param source  原始背景图
     * @param centerX 缺口圆心 X
     * @param centerY 缺口圆心 Y
     * @return 圆形滑块图（ARGB 透明背景）
     */
    private static BufferedImage createCircleSlider(BufferedImage source, int centerX, int centerY) {
        int diameter = 18 * 2;
        BufferedImage slider = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = slider.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 剪裁为圆形
        g.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
        g.drawImage(source, -(centerX - 18), -(centerY - 18), null);

        // 白色半透明描边，增强滑块视觉边界
        g.setColor(new Color(255, 255, 255, 110));
        g.setStroke(new BasicStroke(2));
        g.drawOval(0, 0, diameter - 1, diameter - 1);

        g.dispose();
        return slider;
    }

    /**
     * 将 BufferedImage 编码为 Base64 字符串
     *
     * @param image  图像
     * @param format 图片格式（jpg/png）
     * @return Base64 编码的字符串
     * @throws IOException 编码异常
     */
    private static String imageToBase64(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}
