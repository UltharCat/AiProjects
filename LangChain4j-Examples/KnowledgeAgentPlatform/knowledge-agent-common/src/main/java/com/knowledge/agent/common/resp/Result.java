package com.knowledge.agent.common.resp;

import com.knowledge.agent.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;       // 状态码 (200=成功)
    private String message;     // 消息
    private T data;             // 数据载荷

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.code());
        result.setMessage(ErrorCode.SUCCESS.defaultMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.code(), errorCode.defaultMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.code(), message);
    }

    public boolean isSuccess() {
        return ErrorCode.SUCCESS.code() == code;
    }

}
