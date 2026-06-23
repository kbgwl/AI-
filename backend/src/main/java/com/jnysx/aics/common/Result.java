package com.jnysx.aics.common;

import com.alibaba.fastjson2.JSON;
import java.io.Serializable;

/**
 * 统一响应结果类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200-成功，400-参数错误，401-未授权，500-服务器错误 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 成功（带消息和数据） */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 失败 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败（带状态码） */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 参数错误 */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /** 未授权 */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    // ==================== Getter/Setter ====================

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
