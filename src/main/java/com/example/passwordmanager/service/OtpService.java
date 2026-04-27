package com.example.passwordmanager.service;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateSecret() {
        byte[] randomBytes = new byte[20];
        RANDOM.nextBytes(randomBytes);
        return encodeBase32(randomBytes);
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }

        long counter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int offset = -1; offset <= 1; offset++) {
            if (generateCode(secret, counter + offset).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String secret, long counter) {
        try {
            byte[] key = decodeBase32(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to generate OTP code", ex);
        }
    }

    private String encodeBase32(byte[] bytes) {
        StringBuilder encoded = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte value : bytes) {
            buffer <<= 8;
            buffer |= value & 0xff;
            bitsLeft += 8;

            while (bitsLeft >= 5) {
                encoded.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            encoded.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 31));
        }

        return encoded.toString();
    }

    private byte[] decodeBase32(String encoded) {
        String normalized = encoded.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        ByteBuffer buffer = ByteBuffer.allocate(normalized.length() * 5 / 8 + 1);
        int value = 0;
        int bitsLeft = 0;

        for (char character : normalized.toCharArray()) {
            int index = BASE32_ALPHABET.indexOf(character);
            if (index < 0) {
                throw new IllegalArgumentException("Invalid Base32 secret");
            }
            value = (value << 5) | index;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                buffer.put((byte) ((value >> (bitsLeft - 8)) & 0xff));
                bitsLeft -= 8;
            }
        }

        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);
        return bytes;
    }
}
