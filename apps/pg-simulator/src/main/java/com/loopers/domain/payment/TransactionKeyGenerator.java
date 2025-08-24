package com.loopers.domain.payment;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TransactionKeyGenerator {

    private static final String KEY_TRANSACTION = "TR";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        LocalDateTime now = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return DATETIME_FORMATTER.format(now) + ":" + KEY_TRANSACTION + ":" + uuid;
    }
}
