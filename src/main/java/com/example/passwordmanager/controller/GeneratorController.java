package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.PasswordGeneratorDto;
import com.example.passwordmanager.service.PasswordGeneratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class GeneratorController {

    private final PasswordGeneratorService passwordGeneratorService;

    public GeneratorController(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @GetMapping("/generator")
    public String generatorPage(Model model) {
        model.addAttribute("generator", new PasswordGeneratorDto());
        return "generator/index";
    }

    @PostMapping("/generator")
    public String generatePassword(@ModelAttribute("generator") PasswordGeneratorDto dto,
                                   Model model) {
        model.addAttribute("generator", dto);
        model.addAttribute("generatedPassword", passwordGeneratorService.generate(dto));
        return "generator/index";
    }
}