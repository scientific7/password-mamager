package com.example.passwordmanager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.passwordmanager.dto.ChangePasswordDto;
import com.example.passwordmanager.dto.ProfileDto;
import com.example.passwordmanager.dto.RegisterDto;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.exception.DuplicateEmailException;
import com.example.passwordmanager.repository.UserRepository;
import com.example.passwordmanager.security.UserPrincipal;
import com.example.passwordmanager.service.EmailService;
import com.example.passwordmanager.service.AuditService;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    private static final int PASSWORD_RESET_TOKEN_MINUTES = 30;
    private static final int EMAIL_VERIFICATION_TOKEN_MINUTES = 60;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Transactional
    public void registerUser(RegisterDto dto, String appUrl) {
        String email = dto.getEmail().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("Email already registered");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_USER");
        user.setEmailVerified(true);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(EMAIL_VERIFICATION_TOKEN_MINUTES));

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationTokenHash(hashToken(verificationToken));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        emailService.sendVerificationEmail(user, verificationToken, appUrl);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ProfileDto getProfileDto() {
        User user = getCurrentUser();
        ProfileDto dto = new ProfileDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMfaEnabled(Boolean.TRUE.equals(user.getMfaEnabled()));
        dto.setMfaSecret(user.getMfaSecret());
        return dto;
    }

    @Transactional
    public void updateProfile(ProfileDto dto) {
        User user = getCurrentUser();
        String newEmail = dto.getEmail().trim();

        if (!user.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.findByEmail(newEmail).isPresent()) {
            throw new DuplicateEmailException("Email already registered");
        }

        user.setFullName(dto.getFullName().trim());
        user.setEmail(newEmail);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordDto dto) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public String enableMfa() {
        User user = getCurrentUser();
        if (user.getMfaSecret() == null || user.getMfaSecret().isBlank()) {
            user.setMfaSecret(otpService.generateSecret());
        }
        user.setMfaEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user.getMfaSecret();
    }

    @Transactional
    public void disableMfa() {
        User user = getCurrentUser();
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public String requestPasswordReset(String email) {
        return userRepository.findByEmail(email.trim())
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setPasswordResetTokenHash(hashToken(token));
                    user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_MINUTES));
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return token;
                })
                .orElse(null);
    }

    public User validatePasswordResetToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String tokenHash = hashToken(token);
        return userRepository.findByPasswordResetTokenHash(tokenHash)
                .filter(user -> user.getPasswordResetExpiresAt() != null
                        && user.getPasswordResetExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmNewPassword) {
        if (newPassword == null || !newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        User user = validatePasswordResetToken(token);
        if (user == null) {
            throw new IllegalArgumentException("Reset token is invalid or expired");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String tokenHash = hashToken(token);
        return userRepository.findByEmailVerificationTokenHash(tokenHash)
                .filter(user -> user.getEmailVerificationExpiresAt() != null
                        && user.getEmailVerificationExpiresAt().isAfter(LocalDateTime.now()))
                .map(user -> {
                    user.setEmailVerified(true);
                    user.setEmailVerificationTokenHash(null);
                    user.setEmailVerificationExpiresAt(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void recordFailedLoginAttempt(String email) {
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            if (user.getLockoutExpiresAt() != null && user.getLockoutExpiresAt().isAfter(LocalDateTime.now())) {
                return;
            }
            int attempts = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;
            if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockoutExpiresAt(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                user.setFailedLoginAttempts(0);
            } else {
                user.setFailedLoginAttempts(attempts);
            }
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedLoginAttempts(String email) {
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockoutExpiresAt(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashedBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}

