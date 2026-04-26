package com.github.pkyit.fncaptcha.service.impl;

import com.github.pkyit.fncaptcha.component.TrajectoryAnalysisResult;
import com.github.pkyit.fncaptcha.component.TrajectoryAnalyzer;
import com.github.pkyit.fncaptcha.config.CaptchaConfigProperties;
import com.github.pkyit.fncaptcha.config.CaptchaConfiguration;
import com.github.pkyit.fncaptcha.domain.dto.CaptchaVerifyDTO;
import com.github.pkyit.fncaptcha.domain.dto.Result;
import com.github.pkyit.fncaptcha.domain.dto.TrajectoryPoint;
import com.github.pkyit.fncaptcha.domain.entity.CaptchaImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SliderCaptchaServiceImplTest {

    @Mock
    private CaptchaConfiguration captchaConfiguration;
    @Mock
    private CaptchaConfigProperties captchaConfigProperties;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private TrajectoryAnalyzer trajectoryAnalyzer;
    @Mock
    private RBucket<Object> bucket;

    private SliderCaptchaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SliderCaptchaServiceImpl(
                captchaConfiguration, captchaConfigProperties, redissonClient, trajectoryAnalyzer
        );
    }

    @Test
    void verify_expiredCaptcha_returnsFail() {
        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        CaptchaVerifyDTO dto = new CaptchaVerifyDTO();
        dto.setCaptchaId("expired-id");
        dto.setClientId("user1");
        dto.setSliderX(100);
        dto.setTrajectory(List.of(new TrajectoryPoint()));

        Result<Object> result = service.verify(dto);
        assertEquals("验证码已过期或不存在", result.getMessage());
    }

    @Test
    void verify_correctSliderX_passes() {
        CaptchaImageRepository repo = new CaptchaImageRepository(
                "test-id", "bg", "slider", 150, 80, "user1", 120
        );

        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(repo);
        when(trajectoryAnalyzer.analyze(any())).thenReturn(TrajectoryAnalysisResult.pass());

        CaptchaVerifyDTO dto = new CaptchaVerifyDTO();
        dto.setCaptchaId("test-id");
        dto.setClientId("user1");
        dto.setSliderX(149);
        dto.setTrajectory(mockTrajectory());

        Result<Object> result = service.verify(dto);
        assertEquals("验证通过", result.getMessage());
    }

    @Test
    void verify_wrongSliderX_fails() {
        CaptchaImageRepository repo = new CaptchaImageRepository(
                "test-id", "bg", "slider", 150, 80, "user1", 120
        );

        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(repo);
        when(trajectoryAnalyzer.analyze(any())).thenReturn(TrajectoryAnalysisResult.pass());

        CaptchaVerifyDTO dto = new CaptchaVerifyDTO();
        dto.setCaptchaId("test-id");
        dto.setClientId("user1");
        dto.setSliderX(200);
        dto.setTrajectory(mockTrajectory());

        Result<Object> result = service.verify(dto);
        assertEquals("验证失败，请重试", result.getMessage());
    }

    @Test
    void verify_trajectoryFail_returnsFail() {
        CaptchaImageRepository repo = new CaptchaImageRepository(
                "test-id", "bg", "slider", 150, 80, "user1", 120
        );

        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(repo);
        when(trajectoryAnalyzer.analyze(any())).thenReturn(
                TrajectoryAnalysisResult.fail("速度曲线异常，疑似机器操作")
        );

        CaptchaVerifyDTO dto = new CaptchaVerifyDTO();
        dto.setCaptchaId("test-id");
        dto.setClientId("user1");
        dto.setSliderX(149);
        dto.setTrajectory(mockTrajectory());

        Result<Object> result = service.verify(dto);
        assertTrue(result.getMessage().contains("速度曲线异常"));
    }

    @Test
    void verify_bucketDeletedAfterUse() {
        CaptchaImageRepository repo = new CaptchaImageRepository(
                "test-id", "bg", "slider", 150, 80, "user1", 120
        );

        when(redissonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(repo);
        when(trajectoryAnalyzer.analyze(any())).thenReturn(TrajectoryAnalysisResult.pass());

        CaptchaVerifyDTO dto = new CaptchaVerifyDTO();
        dto.setCaptchaId("test-id");
        dto.setClientId("user1");
        dto.setSliderX(149);
        dto.setTrajectory(mockTrajectory());

        service.verify(dto);
        verify(bucket).delete();
    }

    private List<TrajectoryPoint> mockTrajectory() {
        List<TrajectoryPoint> points = new ArrayList<>();
        long base = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            TrajectoryPoint p = new TrajectoryPoint();
            p.setX(i * 10);
            p.setY(50 + (i % 3));
            p.setTimestamp(base + i * 80L);
            points.add(p);
        }
        return points;
    }
}
