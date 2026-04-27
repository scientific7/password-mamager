package com.example.passwordmanager.controller;

import com.example.passwordmanager.service.VaultService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

    private final VaultService vaultService;

    public SecurityController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping("/security")
    public String securityDashboard(Model model) {
        model.addAttribute("audit", vaultService.buildSecurityAuditForCurrentUser());
        return "security/index";
    }
}
