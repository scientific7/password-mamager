package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.CategoryDto;
import com.example.passwordmanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryService.listForCurrentUser());
        model.addAttribute("category", new CategoryDto());
        return "category/categories";
    }

    @PostMapping("/categories")
    public String addCategory(@Valid @org.springframework.web.bind.annotation.ModelAttribute("category") CategoryDto dto,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.listForCurrentUser());
            return "category/categories";
        }

        categoryService.createForCurrentUser(dto);
        return "redirect:/categories?created";
    }
}