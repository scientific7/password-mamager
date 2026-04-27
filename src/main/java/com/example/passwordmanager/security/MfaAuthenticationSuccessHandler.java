package com.example.passwordmanager.security;

import java.io.IOException;

import com.example.passwordmanager.repository.UserRepository;
import com.example.passwordmanager.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class MfaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    public static final String MFA_VERIFIED_SESSION_KEY = "MFA_VERIFIED";

    private final UserRepository userRepository;
    private final UserService userService;

    public MfaAuthenticationSuccessHandler(UserRepository userRepository,
                                           UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        boolean mfaEnabled = userRepository.findByEmail(authentication.getName())
                .map(user -> Boolean.TRUE.equals(user.getMfaEnabled()))
                .orElse(false);

        userService.resetFailedLoginAttempts(authentication.getName());

        request.getSession(true).setAttribute(MFA_VERIFIED_SESSION_KEY, !mfaEnabled);
        response.sendRedirect(mfaEnabled ? request.getContextPath() + "/mfa" : request.getContextPath() + "/dashboard");
    }
}
