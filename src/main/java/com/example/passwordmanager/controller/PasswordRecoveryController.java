package com.example.passwordmanager.controller;

import com.example.passwordmanager.service.EmailService;
import com.example.passwordmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordRecoveryController {

    private final UserService userService;
    private final EmailService emailService;

    public PasswordRecoveryController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam("email") String email,
                                       HttpServletRequest request,
                                       Model model) {
        String token = userService.requestPasswordReset(email);
        model.addAttribute("message", "If an account exists for that email, a recovery link has been sent.");
        if (token != null) {
            String resetLink = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
                    + request.getContextPath() + "/reset-password?token=" + token;
            emailService.sendPasswordReset(email, resetLink, 30);
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "token", required = false) String token,
                                    Model model) {
        if (token == null || userService.validatePasswordResetToken(token) == null) {
            model.addAttribute("error", "The recovery link is invalid or has expired. Start again.");
            return "auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        try {
            userService.resetPassword(token, password, confirmPassword);
            return "redirect:/login?resetSuccess";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(value = "token", required = false) String token,
                              Model model) {
        if (token == null || !userService.verifyEmailToken(token)) {
            model.addAttribute("error", "Email verification failed or the link has expired.");
            return "auth/verify-email";
        }
        model.addAttribute("success", "Your email has been verified. You can now sign in.");
        return "auth/verify-email";
    }
}
