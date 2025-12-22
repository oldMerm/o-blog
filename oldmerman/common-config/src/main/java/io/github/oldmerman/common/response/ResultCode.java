package io.github.oldmerman.common.response;

import io.github.oldmerman.common.response.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {
    
    // 成功响应
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    PARAM_ERROR(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误 1xxx
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ACCOUNT_LOCKED(1002, "用户账号已锁定"),
    USER_ACCOUNT_INVALID(1003, "用户账号已失效"),
    USERNAME_OR_PASSWORD_ERROR(1004, "用户名或密码错误"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),
    TOKEN_PARSING_FAILED(1007,"Token解析失败"),
    UPLOAD_CONFLICTS(1008, "上传冲突，请稍后重试"),
    SEND_FAIL(1009, "发送失败，请稍后重试"),

    // 数据错误 2xxx
    DATA_NOT_EXIST(2001, "数据不存在"),
    DATA_ALREADY_EXIST(2002, "数据已存在");

    private final Integer code;
    private final String message;
}