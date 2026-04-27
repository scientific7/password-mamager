package com.example.passwordmanager.controller;

import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.security.MfaAuthenticationSuccessHandler;
import com.example.passwordmanager.service.OtpService;
import com.example.passwordmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MfaController {

    private final UserService userService;
    private final OtpService otpService;

    public MfaController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/mfa")
    public String mfaPage(Model model) {
        User user = userService.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            model.addAttribute("error", "Authenticator not configured. Enable OTP in your profile settings.");
            return "auth/mfa";
        }
        model.addAttribute("email", user.getEmail());
        return "auth/mfa";
    }

    @PostMapping("/mfa")
    public String verifyMfa(@RequestParam("code") String code,
                            HttpServletRequest request,
                            Model model) {
        User user = userService.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            model.addAttribute("error", "Authenticator is not configured. Please enable OTP before verifying.");
            return "auth/mfa";
        }

        if (otpService.verifyCode(user.getMfaSecret(), code)) {
            request.getSession(true).setAttribute(MfaAuthenticationSuccessHandler.MFA_VERIFIED_SESSION_KEY, true);
            return "redirect:/dashboard";
        }

        model.addAttribute("email", user.getEmail());
        model.addAttribute("error", "Invalid or expired verification code.");
        return "auth/mfa";
    }
}
