package io.github.oldmerman.common.util;

import cn.hutool.core.lang.Snowflake;

public class IdGenerator {

    public static final Snowflake snowflake = new Snowflake(1);

    public static Long nextId(){
        return snowflake.nextId();
    }
}
