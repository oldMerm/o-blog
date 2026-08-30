package io.github.oldmerman.common.constant;

import java.time.format.DateTimeFormatter;

public class DateTimePatterns {
    public static final DateTimeFormatter DEFAULT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter LOG_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
