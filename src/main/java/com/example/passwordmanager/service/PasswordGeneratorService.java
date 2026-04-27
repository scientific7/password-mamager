package com.example.passwordmanager.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.example.passwordmanager.dto.PasswordGeneratorDto;

@Service
public class PasswordGeneratorService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate(PasswordGeneratorDto dto) {
        StringBuilder chars = new StringBuilder();
        StringBuilder password = new StringBuilder();

        if (dto.isIncludeUppercase()) {
            chars.append(UPPER);
            password.append(randomChar(UPPER));
        }
        if (dto.isIncludeLowercase()) {
            chars.append(LOWER);
            password.append(randomChar(LOWER));
        }
        if (dto.isIncludeNumbers()) {
            chars.append(NUMBERS);
            password.append(randomChar(NUMBERS));
        }
        if (dto.isIncludeSpecialCharacters()) {
            chars.append(SPECIAL);
            password.append(randomChar(SPECIAL));
        }

        if (chars.length() == 0) {
            chars.append(LOWER);
            password.append(randomChar(LOWER));
        }

        int length = Math.max(12, dto.getLength());

        while (password.length() < length) {
            int index = RANDOM.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return shuffle(password);
    }

    private char randomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    private String shuffle(StringBuilder password) {
        for (int i = password.length() - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(j));
            password.setCharAt(j, temp);
        }
        return password.toString();
    }
}
