package com.github.pkyit.fncaptcha.component;

import com.github.pkyit.fncaptcha.domain.dto.TrajectoryPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrajectoryAnalyzerTest {

    private final TrajectoryAnalyzer analyzer = new TrajectoryAnalyzer();

    @Test
    void tooFewPoints_shouldFail() {
        List<TrajectoryPoint> points = new ArrayList<>();
        points.add(point(0, 100, 0));
        points.add(point(10, 100, 100));

        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertFalse(result.isPassed());
        assertEquals("轨迹点数不足", result.getReason());
    }

    @Test
    void tooShortDuration_shouldFail() {
        List<TrajectoryPoint> points = humanLikeTrajectory(50);
        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertFalse(result.isPassed());
    }

    @Test
    void constantSpeed_shouldFail() {
        List<TrajectoryPoint> points = new ArrayList<>();
        for (int i = 0; i <= 30; i++) {
            points.add(point(i * 5, 50, i * 30L));
        }

        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("速度曲线异常"));
    }

    @Test
    void humanLikeTrajectory_shouldPass() {
        List<TrajectoryPoint> points = humanLikeTrajectory(1500);
        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertTrue(result.isPassed(), "人类轨迹应通过验证，但结果: " + result.getReason());
    }

    @Test
    void nonHorizontalMovement_shouldFail() {
        List<TrajectoryPoint> points = new ArrayList<>();
        long base = System.currentTimeMillis();
        for (int i = 0; i <= 20; i++) {
            points.add(point(i * 3, 100 + i * 10, base + i * 100L));
        }

        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertFalse(result.isPassed());
    }

    @Test
    void tooLongDuration_shouldFail() {
        List<TrajectoryPoint> points = new ArrayList<>();
        long base = System.currentTimeMillis();
        for (int i = 0; i <= 10; i++) {
            points.add(point(i * 10, 50, base + i * 1500L));
        }

        TrajectoryAnalysisResult result = analyzer.analyze(points);
        assertFalse(result.isPassed());
    }

    private TrajectoryPoint point(int x, int y, long timestamp) {
        TrajectoryPoint p = new TrajectoryPoint();
        p.setX(x);
        p.setY(y);
        p.setTimestamp(timestamp);
        return p;
    }

    private List<TrajectoryPoint> humanLikeTrajectory(long durationMs) {
        List<TrajectoryPoint> points = new ArrayList<>();
        long base = System.currentTimeMillis();
        int startX = 0;
        int endX = 200;
        int y = 50;

        points.add(point(startX, y, base));

        double totalTime = durationMs;
        double elapsed = 0;
        int lastX = startX;
        while (elapsed < totalTime) {
            elapsed += Math.random() * 40 + 10;
            if (elapsed > totalTime) elapsed = totalTime;
            double progress = elapsed / totalTime;
            int currentX = startX + (int) (Math.pow(progress, 0.5) * (endX - startX));
            if (currentX <= lastX) currentX = lastX + 1;
            lastX = currentX;
            points.add(point(currentX, y + (int) (Math.random() * 4 - 2), base + (long) elapsed));
        }

        return points;
    }
}
