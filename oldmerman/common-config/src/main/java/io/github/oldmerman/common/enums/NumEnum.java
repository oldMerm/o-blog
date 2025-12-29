package io.github.oldmerman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NumEnum implements EnumVal<Long>{

    ACCESS_TOKEN_EXPIRATION(2880L),
    REFRESH_TOKEN_EXPIRATION(86400L),
    ALLOW_ACCESS_TIME(30L),
    USER_ATTR_EXPIRE(120L),
    ;

    private final Long value;
}
