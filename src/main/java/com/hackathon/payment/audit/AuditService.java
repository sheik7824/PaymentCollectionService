package com.hackathon.payment.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Writes in a separate transaction so audit entries survive even if the
     * business transaction rolls back (e.g. failed login).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String action, String entityType, String entityId, String status, String message) {
        auditLogRepository.save(AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .status(status)
                .message(message)
                .build());
    }
}
