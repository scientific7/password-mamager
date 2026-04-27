package com.example.passwordmanager.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.passwordmanager.dto.VaultEntryDto;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.service.CategoryService;
import com.example.passwordmanager.service.UserService;
import com.example.passwordmanager.service.VaultService;

@Controller
public class DashboardController {

    private final VaultService vaultService;
    private final UserService userService;
    private final CategoryService categoryService;

    public DashboardController(VaultService vaultService,
                               UserService userService,
                               CategoryService categoryService) {
        this.vaultService = vaultService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "q", required = false) String query,
                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                            @RequestParam(value = "filter", required = false) String filter,
                            Model model) {
        User currentUser = userService.getCurrentUser();
        List<VaultEntryDto> entries = vaultService.listEntryDtosForCurrentUser(query, categoryId, filter);

        model.addAttribute("user", currentUser);
        model.addAttribute("entries", entries);
        model.addAttribute("categories", categoryService.listForCurrentUser());
        model.addAttribute("query", query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("filter", filter);
        model.addAttribute("totalEntries", entries.size());
        model.addAttribute("favoriteCount",
                entries.stream().filter(e -> Boolean.TRUE.equals(e.getFavorite())).count());
        model.addAttribute("weakCount",
                entries.stream().filter(e -> "Weak".equals(e.getPasswordStrengthLabel())).count());
        model.addAttribute("reusedCount",
                entries.stream().filter(e -> Boolean.TRUE.equals(e.getPasswordReused())).count());
        model.addAttribute("oldCount",
                entries.stream().filter(e -> Boolean.TRUE.equals(e.getPasswordOld())).count());
        model.addAttribute("recentEntries", vaultService.listRecentDtosForCurrentUser(4));
        model.addAttribute("mostUsedEntries", vaultService.listMostUsedDtosForCurrentUser(4));
        model.addAttribute("securityAudit", vaultService.buildSecurityAuditForCurrentUser());

        return "dashboard/index";
    }
}
