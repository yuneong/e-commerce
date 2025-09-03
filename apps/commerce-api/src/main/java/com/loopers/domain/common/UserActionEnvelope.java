package com.loopers.domain.common;

import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActionEnvelope<T>(
        String eventId,
        String traceId,
        String userId,
        String actionType,
        T payload,
        LocalDateTime occurredAt
) {

    public static <T>UserActionEnvelope of(
            String actionType,
            String userId,
            T payload
    ) {
        return new UserActionEnvelope<>(
                UUID.randomUUID().toString(),
                MDC.get("traceId"),
                MDC.get("userId") == null ? userId : MDC.get("userId"),
                actionType,
                payload,
                LocalDateTime.now()
        );
    }

}
