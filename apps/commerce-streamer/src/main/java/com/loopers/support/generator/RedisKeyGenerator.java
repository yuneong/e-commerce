package com.loopers.support.generator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class RedisKeyGenerator {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private RedisKeyGenerator() {}

    public static String buildKey(String prefix, LocalDate date) {
        return prefix + date.format(DAY);
    }

    public static String todayKey(String prefix) {
        return buildKey(prefix, LocalDate.now(KST));
    }

}
