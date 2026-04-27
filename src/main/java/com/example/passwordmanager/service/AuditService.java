package com.example.passwordmanager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.passwordmanager.entity.AuditLog;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.repository.AuditLogRepository;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    public AuditService(AuditLogRepository auditLogRepository,
                        UserService userService) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
    }

    @Transactional
    public void log(String action, String targetType, Long targetId, String ipAddress) {
        User user = userService.getCurrentUser();

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    public List<AuditLog> getCurrentUserLogs() {
        User user = userService.getCurrentUser();
        return auditLogRepository.findByUserIdOrderByTimestampDesc(user.getId());
    }
}
