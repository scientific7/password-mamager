package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.VaultEntryDto;
import com.example.passwordmanager.service.AuditService;
import com.example.passwordmanager.service.CategoryService;
import com.example.passwordmanager.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vault")
public class VaultController {

    private final VaultService vaultService;
    private final CategoryService categoryService;
    private final AuditService auditService;

    public VaultController(VaultService vaultService,
                           CategoryService categoryService,
                           AuditService auditService) {
        this.vaultService = vaultService;
        this.categoryService = categoryService;
        this.auditService = auditService;
    }

    @GetMapping("/new")
    public String newEntryForm(Model model) {
        model.addAttribute("entry", new VaultEntryDto());
        model.addAttribute("categories", categoryService.listForCurrentUser());
        return "vault/new-entry";
    }

    @PostMapping
    public String createEntry(@Valid @ModelAttribute("entry") VaultEntryDto dto,
                              BindingResult result,
                              Model model,
                              HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.listForCurrentUser());
            return "vault/new-entry";
        }

        Long entryId = vaultService.createEntryForCurrentUser(dto);
        auditService.log("ADD_ENTRY", "VAULT_ENTRY", entryId, request.getRemoteAddr());

        return "redirect:/dashboard?created";
    }

    @GetMapping("/{id}")
    public String viewEntry(@PathVariable Long id, Model model, HttpServletRequest request) {
        VaultEntryDto entry = vaultService.getEntryDtoForView(id);
        model.addAttribute("entry", entry);
        auditService.log("VIEW_ENTRY", "VAULT_ENTRY", id, request.getRemoteAddr());
        return "vault/view-entry";
    }

    @GetMapping("/{id}/edit")
    public String editEntryForm(@PathVariable Long id, Model model) {
        model.addAttribute("entry", vaultService.getEntryDtoForEdit(id));
        model.addAttribute("categories", categoryService.listForCurrentUser());
        return "vault/edit-entry";
    }

    @PostMapping("/{id}/edit")
    public String updateEntry(@PathVariable Long id,
                              @Valid @ModelAttribute("entry") VaultEntryDto dto,
                              BindingResult result,
                              Model model,
                              HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.listForCurrentUser());
            return "vault/edit-entry";
        }

        vaultService.updateEntryForCurrentUser(id, dto);
        auditService.log("UPDATE_ENTRY", "VAULT_ENTRY", id, request.getRemoteAddr());

        return "redirect:/vault/" + id + "?updated";
    }

    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id, HttpServletRequest request) {
        vaultService.deleteEntryForCurrentUser(id);
        auditService.log("DELETE_ENTRY", "VAULT_ENTRY", id, request.getRemoteAddr());
        return "redirect:/dashboard?deleted";
    }
}
