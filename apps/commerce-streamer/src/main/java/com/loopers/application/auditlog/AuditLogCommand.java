package com.loopers.application.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogCommand (
        String eventId,
        String eventType,
        String traceId,
        String userId,
        String actionType,
        Map<String,Object> payload,
        LocalDateTime occurredAt
) {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AuditLogCommand of(
            String payload,
            String eventType
    ) {
        try {
            objectMapper.registerModule(new JavaTimeModule()); //LocalDateTime 직렬화 이슈 해결 예전 레디스와 같은 문제
            AuditLogCommand temp = objectMapper.readValue(payload, AuditLogCommand.class);
            return new AuditLogCommand(
                    temp.eventId(),
                    eventType,
                    temp.traceId(),
                    temp.userId(),
                    temp.actionType(),
                    temp.payload(),
                    temp.occurredAt()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse payload", e);
        }

    }

}
