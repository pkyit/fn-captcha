package com.github.pkyit.fncaptcha.component;

import com.github.pkyit.fncaptcha.domain.dto.TrajectoryPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 滑动轨迹行为分析器
 * <p>分析用户鼠标轨迹数据，从点数、时长、速度变化、方向一致性等多个维度
 * 判断操作是否为人类行为，用于抵御自动化脚本/机器人攻击。</p>
 */
@Component
public class TrajectoryAnalyzer {

    /** 轨迹最少采样点数 */
    private static final int MIN_POINTS = 5;

    /** 滑动最短通过时间（毫秒），低于此值视为机器操作 */
    private static final long MIN_DURATION_MS = 200;

    /** 滑动最长通过时间（毫秒），超过此值视为超时 */
    private static final long MAX_DURATION_MS = 10_000;

    /**
     * 速度变异系数最小值
     * <p>人类滑动有加速→匀速→减速的过程，速度波动大（CV > 0.3）；
     * 机器人轨迹速度均匀（CV 接近 0）。</p>
     */
    private static final double MIN_SPEED_CV = 0.3;

    /**
     * 方向一致性最小比值
     * <p>水平滑动时 X 轴位移应占主导地位（> 70%），
     * 垂直抖动过多的轨迹不合常理。</p>
     */
    private static final double MIN_DIRECTION_RATIO = 0.7;

    /**
     * 分析轨迹数据并返回分析结果
     *
     * @param trajectory 前端采集的鼠标轨迹点列表（按时间排序）
     * @return 分析结果（通过/失败及原因）
     */
    public TrajectoryAnalysisResult analyze(List<TrajectoryPoint> trajectory) {
        if (trajectory.size() < MIN_POINTS) {
            return TrajectoryAnalysisResult.fail("轨迹点数不足");
        }

        long duration = trajectory.get(trajectory.size() - 1).getTimestamp()
                      - trajectory.get(0).getTimestamp();

        if (duration < MIN_DURATION_MS) {
            return TrajectoryAnalysisResult.fail("滑动速度过快，疑似机器操作");
        }
        if (duration > MAX_DURATION_MS) {
            return TrajectoryAnalysisResult.fail("滑动超时，请重试");
        }

        double speedCV = calculateSpeedCoefficientOfVariation(trajectory);
        if (speedCV < MIN_SPEED_CV) {
            return TrajectoryAnalysisResult.fail("速度曲线异常，疑似机器操作");
        }

        double directionRatio = calculateDirectionConsistency(trajectory);
        if (directionRatio < MIN_DIRECTION_RATIO) {
            return TrajectoryAnalysisResult.fail("滑动方向异常");
        }

        return TrajectoryAnalysisResult.pass();
    }

    /**
     * 计算速度变异系数（Coefficient of Variation）
     * <p>CV = 标准差 / 均值。人类操作的速度 CV 通常较高，
     * 因为存在加速→匀速→减速的自然过程。</p>
     *
     * @param pts 轨迹点
     * @return 速度变异系数（值越大表示速度波动越大）
     */
    private double calculateSpeedCoefficientOfVariation(List<TrajectoryPoint> pts) {
        List<Double> speeds = new ArrayList<>();
        for (int i = 1; i < pts.size(); i++) {
            long dt = pts.get(i).getTimestamp() - pts.get(i - 1).getTimestamp();
            if (dt <= 0) {
                continue;
            }
            double dx = pts.get(i).getX() - pts.get(i - 1).getX();
            double dy = pts.get(i).getY() - pts.get(i - 1).getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            speeds.add(dist / dt);
        }

        if (speeds.size() < 2) {
            return 0;
        }

        double mean = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        if (mean == 0) {
            return 0;
        }

        double variance = speeds.stream().mapToDouble(s -> Math.pow(s - mean, 2)).average().orElse(0);
        return Math.sqrt(variance) / mean;
    }

    /**
     * 计算方向一致性
     * <p>水平方向总位移 / 总欧氏距离。滑块验证是水平拖动操作，
     * X 轴位移应占总位移的绝大部分。</p>
     *
     * @param pts 轨迹点
     * @return 方向一致性比值（接近 1 表示基本水平移动）
     */
    private double calculateDirectionConsistency(List<TrajectoryPoint> pts) {
        double totalHorizontal = 0;
        double totalDistance = 0;

        for (int i = 1; i < pts.size(); i++) {
            double dx = Math.abs(pts.get(i).getX() - pts.get(i - 1).getX());
            double dy = Math.abs(pts.get(i).getY() - pts.get(i - 1).getY());
            double dist = Math.sqrt(dx * dx + dy * dy);
            totalHorizontal += dx;
            totalDistance += dist;
        }

        if (totalDistance == 0) {
            return 0;
        }

        return totalHorizontal / totalDistance;
    }
}
