package com.example.passwordmanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.passwordmanager.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from:no-reply@example.com}")
    private String from;

    @Value("${app.verification.expiry-minutes:60}")
    private int verificationExpiryMinutes;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPasswordReset(String to, String resetLink, int expiryMinutes) {
        if (to == null || to.isBlank() || resetLink == null || resetLink.isBlank()) {
            log.warn("Password reset email request missing recipient or link.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("Password Recovery for Password Manager");

            Context context = new Context();
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryMinutes", expiryMinutes);
            String html = templateEngine.process("email/password-reset", context);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send password reset email to {}", to, ex);
        }
    }

    public void sendVerificationEmail(User user, String verificationToken, String appUrl) {
        if (user == null || user.getEmail() == null || verificationToken == null) {
            log.warn("Email verification request missing user, email, or token.");
            return;
        }

        try {
            String verificationLink = appUrl + "/verify-email?token=" + verificationToken;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(from);
            helper.setSubject("Verify Your Email for Password Manager");

            Context context = new Context();
            context.setVariable("fullName", user.getFullName());
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("expiryMinutes", verificationExpiryMinutes);
            String html = templateEngine.process("email/verify-email", context);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send email verification to {}", user.getEmail(), ex);
        }
    }
}
