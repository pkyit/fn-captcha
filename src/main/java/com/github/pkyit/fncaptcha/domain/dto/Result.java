package com.github.pkyit.fncaptcha.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统一 API 响应结果封装
 * <p>所有接口返回统一的数据格式，包含状态码、消息和数据。</p>
 *
 * @param <T> 数据类型
 */
@Data
@Builder
public class Result<T> implements java.io.Serializable {
    private static final long serialVersionUID = 92294833294894L;

    /** 状态码（200 成功，400 业务失败） */
    private int code;

    /** 提示消息 */
    private String message;

    /** 返回数据 */
    private T data;

    /**
     * 成功响应
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(String message, T data) {
        return Result.<T>builder().code(200).message(message).data(data).build();
    }

    /**
     * 成功响应（无数据）
     *
     * @param message 成功消息
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(String message) {
        return Result.<T>builder().code(200).message(message).build();
    }

    /**
     * 失败响应
     *
     * @param message 失败原因
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(String message) {
        return Result.<T>builder().code(400).message(message).build();
    }
}
