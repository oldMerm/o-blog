package io.github.oldmerman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NumEnum implements EnumVal<Long>{

    ALLOW_ACCESS_TIME(30L),
    USER_ATTR_EXPIRE(120L),
    ARTICLE_EXPIRE_TIME(7L),
    ;

    private final Long value;
}
