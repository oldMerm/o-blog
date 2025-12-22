package io.github.oldmerman.common.exception;

import io.github.oldmerman.common.response.IResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(IResultCode resultCode, Integer traceId) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    public BusinessException(IResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}