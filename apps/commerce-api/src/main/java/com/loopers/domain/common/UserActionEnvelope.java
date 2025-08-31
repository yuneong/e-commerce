package com.loopers.domain.common;

import org.slf4j.MDC;

import java.time.LocalDateTime;

public record UserActionEnvelope<T>(
        String traceId,
        String userId,
        String actionType,
        T payload,
        LocalDateTime occurredAt
) {

    public static <T>UserActionEnvelope of(
            String actionType,
            T payload
    ) {
        return new UserActionEnvelope<>(
                MDC.get("traceId"),
                MDC.get("userId"),
                actionType,
                payload,
                LocalDateTime.now()
        );
    }

}
