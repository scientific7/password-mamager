package com.example.passwordmanager.dto;

import java.util.ArrayList;
import java.util.List;

public class SecurityAuditDto {

    private int totalEntries;
    private int weakCount;
    private int reusedCount;
    private int oldCount;
    private int favoriteCount;
    private int healthScore;
    private String healthLabel;
    private List<VaultEntryDto> weakEntries = new ArrayList<>();
    private List<VaultEntryDto> reusedEntries = new ArrayList<>();
    private List<VaultEntryDto> oldEntries = new ArrayList<>();

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getWeakCount() {
        return weakCount;
    }

    public void setWeakCount(int weakCount) {
        this.weakCount = weakCount;
    }

    public int getReusedCount() {
        return reusedCount;
    }

    public void setReusedCount(int reusedCount) {
        this.reusedCount = reusedCount;
    }

    public int getOldCount() {
        return oldCount;
    }

    public void setOldCount(int oldCount) {
        this.oldCount = oldCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getHealthLabel() {
        return healthLabel;
    }

    public void setHealthLabel(String healthLabel) {
        this.healthLabel = healthLabel;
    }

    public List<VaultEntryDto> getWeakEntries() {
        return weakEntries;
    }

    public void setWeakEntries(List<VaultEntryDto> weakEntries) {
        this.weakEntries = weakEntries;
    }

    public List<VaultEntryDto> getReusedEntries() {
        return reusedEntries;
    }

    public void setReusedEntries(List<VaultEntryDto> reusedEntries) {
        this.reusedEntries = reusedEntries;
    }

    public List<VaultEntryDto> getOldEntries() {
        return oldEntries;
    }

    public void setOldEntries(List<VaultEntryDto> oldEntries) {
        this.oldEntries = oldEntries;
    }
}
