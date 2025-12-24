package io.github.oldmerman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NumEnum implements EnumVal{

    ACCESS_TOKEN_EXPIRATION(2880),
    REFRESH_TOKEN_EXPIRATION(86400),
    ALLOW_ACCESS_TIME(30),
    ;


    private final long value;
}
