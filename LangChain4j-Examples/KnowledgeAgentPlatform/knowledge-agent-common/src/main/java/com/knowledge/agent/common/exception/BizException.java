package com.knowledge.agent.common.exception;

import lombok.Getter;

/**
 * 业务异常，用于显示抛出业务异常信息
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
