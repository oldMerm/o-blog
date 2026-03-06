package io.github.oldmerman.common.constant;

import java.util.Map;

public class BusinessMap {

    public static final Map<Byte, String> USER_TYPE_MAP = Map.of(
            (byte)1,"admin",
            (byte)2,"user"
    );
}
