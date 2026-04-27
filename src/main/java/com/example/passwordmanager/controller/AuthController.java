package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.RegisterDto;
import com.example.passwordmanager.exception.DuplicateEmailException;
import com.example.passwordmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("user") RegisterDto dto,
                                 BindingResult result,
                                 HttpServletRequest request) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            String appUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                appUrl += ":" + request.getServerPort();
            }
            appUrl += request.getContextPath();
            userService.registerUser(dto, appUrl);
        } catch (DuplicateEmailException ex) {
            result.rejectValue("email", "error.user", ex.getMessage());
            return "auth/register";
        } catch (IllegalArgumentException ex) {
            result.rejectValue("confirmPassword", "error.user", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}
