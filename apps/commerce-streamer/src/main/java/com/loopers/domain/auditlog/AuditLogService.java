package com.loopers.domain.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.auditlog.AuditLogCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveAuditLog(AuditLogCommand command) {
        // 기존 Map<String, Object>를 다시 db에 넣기 위해서 String으로 변환
        String payloadJson = "";
        try {
            payloadJson = objectMapper.writeValueAsString(command.payload());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        AuditLog auditLog = AuditLog.create(
                command.eventId(),
                command.eventType(),
//                command.traceId(),
                command.userId(),
                payloadJson
        );

        auditLogRepository.save(auditLog);
    }

}
