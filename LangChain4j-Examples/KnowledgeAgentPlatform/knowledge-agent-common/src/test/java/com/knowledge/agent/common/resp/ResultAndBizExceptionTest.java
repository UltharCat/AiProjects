package com.knowledge.agent.common.resp;

import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultAndBizExceptionTest {

    @Test
    void shouldBuildSuccessResultUsingSharedStatusCode() {
        Result<String> result = Result.success("ok");

        assertEquals(ErrorCode.SUCCESS.code(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.defaultMessage(), result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    void shouldCreateBizExceptionFromSharedErrorCode() {
        BizException exception = new BizException(ErrorCode.UNAUTHORIZED, "Missing bearer token");

        assertEquals(ErrorCode.UNAUTHORIZED.code(), exception.getCode());
        assertEquals("Missing bearer token", exception.getMessage());
    }
}
