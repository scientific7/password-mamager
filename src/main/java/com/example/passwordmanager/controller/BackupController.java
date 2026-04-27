package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.BackupExportDto;
import com.example.passwordmanager.service.AuditService;
import com.example.passwordmanager.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackupController {

    private final VaultService vaultService;
    private final AuditService auditService;

    public BackupController(VaultService vaultService, AuditService auditService) {
        this.vaultService = vaultService;
        this.auditService = auditService;
    }

    @GetMapping("/backup/export")
    public ResponseEntity<BackupExportDto> exportVault(HttpServletRequest request) {
        BackupExportDto backup = vaultService.exportVaultForCurrentUser();
        auditService.log("EXPORT_BACKUP", "VAULT", null, request.getRemoteAddr());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=password-manager-backup.json")
                .body(backup);
    }
}
