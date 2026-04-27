package com.example.passwordmanager.dto;

import java.util.List;

public class PasswordHealthDto {

    private final int score;
    private final String label;
    private final boolean reused;
    private final boolean old;
    private final List<String> hints;

    public PasswordHealthDto(int score, String label, boolean reused, boolean old, List<String> hints) {
        this.score = score;
        this.label = label;
        this.reused = reused;
        this.old = old;
        this.hints = hints;
    }

    public int getScore() {
        return score;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReused() {
        return reused;
    }

    public boolean isOld() {
        return old;
    }

    public List<String> getHints() {
        return hints;
    }
}
