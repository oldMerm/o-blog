package io.github.oldmerman.common.enums;

import io.github.oldmerman.common.response.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusErrorCode implements IResultCode {

    TOKEN_EXPIRED(1001,"令牌过期，请刷新页面或重新登录"),
    TOKEN_PARSING_FAILED(1002, "令牌解析出错"),

    EMAIL_WRONG_FORMAT(1023, "邮箱格式错误"),
    EMAIL_SEND_FAILED(1024, "邮件发送失败"),
    ERROR_EMAIL_CODE(1025, "邮件认证码错误"),
    ERROR_VERIFY_CODE(1026, "验证码错误或已过期"),

    USERNAME_WRONG_FORMAT(1047, "用户名格式错误"),
    PASSWORD_WRONG_FORMAT(1048, "密码格式错误"),
    LENGTH_EXCEEDS_LIMIT(1049, "长度超过限制"),

    ENCRYPTION_FAILED(1089, "加密失败"),
    UPLOAD_FAILED(1090, "文件上传失败"),
    FILE_EXT_FAILED(1091, "文件格式错误"),
    FILE_LEN_FAILED(1092, "文件上传出现意外错误"),
    FILE_DECR_UNEXIST(1093, "文件描述不详细"),
    FILE_UNEXIST(1094, "文件不存在"),
    ;

    private final Integer code;
    private final String message;
}
