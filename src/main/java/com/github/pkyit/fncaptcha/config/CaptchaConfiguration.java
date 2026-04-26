package com.github.pkyit.fncaptcha.config;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.pkyit.fncaptcha.util.CaptchaImageUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证码图片配置及验证
 * <p>启动时校验所有背景图片的合法性（格式、大小、分辨率），
 * 并提供随机取图功能供 {@link CaptchaImageUtils} 使用。</p>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({CaptchaConfigProperties.class})
public class CaptchaConfiguration {

    private final CaptchaConfigProperties captchaConfigProperties;
    private final List<File> imageFiles = new ArrayList<>();

    public CaptchaConfiguration(CaptchaConfigProperties captchaConfigProperties) {
        this.captchaConfigProperties = captchaConfigProperties;
    }

    /**
     * 启动时验证所有背景图片
     * <p>逐张检查图片是否存在、是否为 PNG 格式、大小不超过 50KB、分辨率是否 300×150。</p>
     *
     * @throws IOException 图片读取异常
     */
    @PostConstruct
    public void checkImageValidity() throws IOException {
        String imagePath = captchaConfigProperties.getImagePath();
        if (imagePath.startsWith("classpath:")) {
            imagePath = imagePath.substring("classpath:".length());
            for (int i = 1; i <= captchaConfigProperties.getImageCount(); i++) {
                ClassPathResource resource = new ClassPathResource(imagePath + i + ".png");
                if (!resource.exists()) {
                    throw new IllegalStateException("文件不存在：" + resource.getPath());
                }
                if (!"png".equals(FileTypeUtil.getType(resource.getFile()))) {
                    throw new IllegalStateException("文件不是png格式：" + resource.getPath());
                }
                if (resource.getFile().length() > 50 * 1024) {
                    throw new IllegalStateException("文件必须小于50Kb：" + resource.getPath());
                }
                BufferedImage image = ImageIO.read(resource.getInputStream());
                if (image.getWidth() != 300 || image.getHeight() != 150) {
                    throw new IllegalStateException("图片分辨率必须为300px * 150px：" + resource.getPath());
                }
                imageFiles.add(resource.getFile());
            }
        } else {
            for (int i = 1; i <= captchaConfigProperties.getImageCount(); i++) {
                File file = new File(imagePath, i + ".png");
                if (!file.exists()) {
                    throw new IllegalStateException("文件不存在：" + file.getAbsolutePath());
                }
                if (!"png".equals(FileTypeUtil.getType(file))) {
                    throw new IllegalStateException("文件不是png格式：" + file.getAbsolutePath());
                }
                if (file.length() > 50 * 1024) {
                    throw new IllegalStateException("文件必须小于50Kb：" + file.getPath());
                }
                BufferedImage image = ImageIO.read(file);
                if (image.getWidth() != 300 || image.getHeight() != 150) {
                    throw new IllegalStateException("图片分辨率必须为300px * 150px：" + file.getPath());
                }
                imageFiles.add(file);
            }
        }
        if (imageFiles.isEmpty()) {
            throw new IllegalStateException("没有加载到任何验证码图片");
        }
        log.info("验证码图片加载完成，共加载{}张图片", imageFiles.size());
    }

    /**
     * 随机获取一张背景图片文件
     *
     * @return 随机选中的图片文件
     */
    public File getRandomImageFile() {
        return imageFiles.get(RandomUtil.randomInt(imageFiles.size()));
    }
}
