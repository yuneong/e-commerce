package com.loopers.application.auditlog;

import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.eventHandled.EventHandledDomainType;
import com.loopers.domain.eventHandled.EventHandledService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogFacade {

    private final AuditLogService auditLogService;
    private final EventHandledService eventHandledService;
    private final static EventHandledDomainType DOMAIN_TYPE = EventHandledDomainType.AUDIT_LOG;

    public void processAuditLog(AuditLogCommand command) {
        if (eventHandledService.isEventHandled(command.eventId())) {
            return;
        }

        auditLogService.saveAuditLog(command);

        eventHandledService.saveEventHandled(command.eventId(), DOMAIN_TYPE, command.eventType());
    }

}
