package com.github.pkyit.fncaptcha.domain.consts;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证码图片常量定义
 * <p>图片目录、数量、格式等静态配置。
 * 优先级低于 {@code CaptchaConfigProperties}（从配置文件读取）。</p>
 */
public class CaptchaImageConst {

    /** 图片资源目录（classpath 下） */
    @Getter @Setter
    public static String IMAGE_DIR = "bg_images/";

    /** 图片总数量 */
    @Getter @Setter
    public static int IMAGE_COUNT = 40;

    /** 图片文件后缀 */
    @Getter @Setter
    public static String IMAGE_TYPE = ".png";

    /** 图片名称列表（在静态初始化块中生成） */
    @Getter @Setter
    public static List<String> IMAGE_NAMES;

    /** 验证码缓存初始化的分布式锁 key */
    public static final String CACHE_INIT_LOCK_KEY = "fncaptcha:lock:cache:warmup";

    static {
        IMAGE_NAMES = new ArrayList<>(IMAGE_COUNT);
        for (int i = 1; i <= IMAGE_COUNT; i++) {
            IMAGE_NAMES.add(i + IMAGE_TYPE);
        }
    }
}
