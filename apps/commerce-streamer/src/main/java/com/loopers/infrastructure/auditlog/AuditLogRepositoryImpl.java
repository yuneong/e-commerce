package com.loopers.infrastructure.auditlog;

import com.loopers.domain.auditlog.AuditLog;
import com.loopers.domain.auditlog.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogJpaRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogJpaRepository.findAll();
    }

}
