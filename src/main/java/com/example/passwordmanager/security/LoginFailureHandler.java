package com.example.passwordmanager.security;

import java.io.IOException;

import com.example.passwordmanager.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    private final UserService userService;

    public LoginFailureHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");
        if (email != null && !email.isBlank()) {
            userService.recordFailedLoginAttempt(email);
            log.warn("Authentication failed for user {}: {}", email, exception.getMessage());
        }

        String redirectUrl = request.getContextPath() + "/login?error";
        if (exception instanceof LockedException) {
            redirectUrl = request.getContextPath() + "/login?error=locked";
        } else if (exception instanceof DisabledException) {
            redirectUrl = request.getContextPath() + "/login?error=notVerified";
        } else {
            redirectUrl = request.getContextPath() + "/login?error=badCredentials";
        }

        response.sendRedirect(redirectUrl);
    }
}
