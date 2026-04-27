package com.example.passwordmanager.controller;

import com.example.passwordmanager.dto.ChangePasswordDto;
import com.example.passwordmanager.dto.ProfileDto;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.exception.DuplicateEmailException;
import com.example.passwordmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/settings")
    public String settingsPage(Model model) {
        model.addAttribute("profile", userService.getProfileDto());
        model.addAttribute("changePassword", new ChangePasswordDto());
        return "profile/settings";
    }

    @PostMapping("/profile/settings")
    public String updateProfile(@Valid @org.springframework.web.bind.annotation.ModelAttribute("profile") ProfileDto dto,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("changePassword", new ChangePasswordDto());
            return "profile/settings";
        }

        try {
            userService.updateProfile(dto);
        } catch (DuplicateEmailException ex) {
            result.rejectValue("email", "error.profile", ex.getMessage());
            model.addAttribute("changePassword", new ChangePasswordDto());
            return "profile/settings";
        }

        return "redirect:/profile/settings?updated";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @org.springframework.web.bind.annotation.ModelAttribute("changePassword") ChangePasswordDto dto,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("profile", userService.getProfileDto());
            return "profile/settings";
        }

        try {
            userService.changePassword(dto);
        } catch (IllegalArgumentException ex) {
            String field = ex.getMessage().startsWith("New passwords")
                    ? "confirmNewPassword"
                    : "currentPassword";
            result.rejectValue(field, "error.changePassword", ex.getMessage());
            model.addAttribute("profile", userService.getProfileDto());
            return "profile/settings";
        }

        return "redirect:/profile/settings?passwordChanged";
    }

    @PostMapping("/profile/mfa/enable")
    public String enableMfa() {
        userService.enableMfa();
        return "redirect:/profile/settings?mfaEnabled";
    }

    @PostMapping("/profile/mfa/disable")
    public String disableMfa() {
        userService.disableMfa();
        return "redirect:/profile/settings?mfaDisabled";
    }

    @GetMapping("/profile/mfa/qrcode")
    public ResponseEntity<byte[]> getQrCode() throws Exception {
        User user = userService.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getMfaEnabled()) || user.getMfaSecret() == null) {
            return ResponseEntity.notFound().build();
        }
        String url = "otpauth://totp/PasswordManager:" + user.getEmail() + "?secret=" + user.getMfaSecret() + "&issuer=PasswordManager";
        BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(baos.toByteArray());
    }
}
