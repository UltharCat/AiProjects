package com.knowledge.agent.common.handler;

import com.knowledge.agent.common.exception.BusinessException;
import com.knowledge.agent.common.resp.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception occurred: ", e);
        return Result.error( 500,"An unexpected error occurred.");
    }

}
