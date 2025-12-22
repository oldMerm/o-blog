package io.github.oldmerman.web.constant;

import io.github.oldmerman.common.response.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebErrorCode implements IResultCode {

    TOKEN_EXPIRED(1001,"令牌过期"),
    TOKEN_PARSING_FAILED(1002, "令牌解析出错"),

    WRONG_EMAIL_FORMAT(1003, "邮箱格式错误"),

    ;

    private final Integer code;
    private final String message;
}
