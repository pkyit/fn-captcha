package com.github.pkyit.fncaptcha.domain.dto;

import lombok.Data;

/**
 * 鼠标滑动轨迹点
 * <p>前端在用户滑动过程中采集的采样点，包含鼠标位置和时间戳，
 * 用于服务端行为分析判断是否为人类操作。</p>
 */
@Data
public class TrajectoryPoint implements java.io.Serializable {
    private static final long serialVersionUID = 62294833294894L;

    /** 鼠标 X 坐标 */
    private int x;

    /** 鼠标 Y 坐标 */
    private int y;

    /** 采样时间戳（毫秒） */
    private long timestamp;
}
