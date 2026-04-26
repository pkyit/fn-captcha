package com.github.pkyit.fncaptcha.component;

import lombok.Builder;
import lombok.Data;

/**
 * 轨迹分析结果
 * <p>封装 {@link TrajectoryAnalyzer} 对用户滑动轨迹的分析结论，
 * 包含是否通过及未通过的原因描述。</p>
 */
@Data
@Builder
public class TrajectoryAnalysisResult {
    /** 是否通过行为分析 */
    private boolean passed;

    /** 未通过的原因 */
    private String reason;

    /**
     * 创建通过结果
     *
     * @return 通过的分析结果
     */
    public static TrajectoryAnalysisResult pass() {
        return TrajectoryAnalysisResult.builder().passed(true).build();
    }

    /**
     * 创建失败结果
     *
     * @param reason 失败原因
     * @return 失败的分析结果
     */
    public static TrajectoryAnalysisResult fail(String reason) {
        return TrajectoryAnalysisResult.builder().passed(false).reason(reason).build();
    }
}
