package io.github.oldmerman.common.enums;

import io.github.oldmerman.common.response.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusErrorCode implements IResultCode {

    TOKEN_EXPIRED(1001,"令牌过期，请刷新页面或重新登录"),
    TOKEN_PARSING_FAILED(1002, "令牌解析出错"),

    EMAIL_WRONG_FORMAT(1003, "邮箱格式错误"),
    EMAIL_SEND_FAILED(1004, "邮件发送失败"),
    ERROR_EMAIL_CODE(1005, "邮件认证码错误"),
    ERROR_VERIFY_CODE(1006, "验证码错误或已过期"),

    USERNAME_WRONG_FORMAT(1007, "用户名格式错误"),
    PASSWORD_WRONG_FORMAT(1008, "密码格式错误"),

    ENCRYPTION_FAILED(1009, "加密失败"),
    UPLOAD_FAILED(1010, "文件上传失败"),
    FILE_EXT_FAILED(1011, "文件格式错误"),
            ;

    private final Integer code;
    private final String message;
}
