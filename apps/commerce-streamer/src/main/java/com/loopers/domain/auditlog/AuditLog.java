package com.loopers.domain.auditlog;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_log")
public class AuditLog {

    @Id private String eventId;
    private String eventType;
//    private String traceId;
    private String userId;
    private String payload;
    private LocalDateTime sentAt;

    public static AuditLog create(
            String eventId,
            String eventType,
//            String traceId,
            String userId,
            String payload
    ) {
        AuditLog auditLog = new AuditLog();

        auditLog.eventId = eventId;
        auditLog.eventType = eventType;
//        auditLog.traceId = traceId;
        auditLog.userId = userId;
        auditLog.payload = payload;
        auditLog.sentAt = LocalDateTime.now();

        return auditLog;
    }

}
