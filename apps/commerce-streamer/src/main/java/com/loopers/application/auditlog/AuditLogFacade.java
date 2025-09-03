package com.loopers.application.auditlog;

import com.loopers.domain.auditlog.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogFacade {

    private final AuditLogService auditLogService;

    public void processAuditLog(AuditLogCommand command) {
        auditLogService.saveAuditLog(command);
    }

}
