package com.example.passwordmanager.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VaultEntryDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String websiteUrl;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    private String notes;

    private Long categoryId;

    private String categoryName;

    private Boolean favorite = false;

    private String passwordStrengthLabel;

    private int passwordStrengthScore;

    private Boolean passwordReused = false;

    private Boolean passwordOld = false;

    private LocalDateTime lastAccessedAt;

    private Integer accessCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public String getPasswordStrengthLabel() {
        return passwordStrengthLabel;
    }

    public void setPasswordStrengthLabel(String passwordStrengthLabel) {
        this.passwordStrengthLabel = passwordStrengthLabel;
    }

    public int getPasswordStrengthScore() {
        return passwordStrengthScore;
    }

    public void setPasswordStrengthScore(int passwordStrengthScore) {
        this.passwordStrengthScore = passwordStrengthScore;
    }

    public Boolean getPasswordReused() {
        return passwordReused;
    }

    public void setPasswordReused(Boolean passwordReused) {
        this.passwordReused = passwordReused;
    }

    public Boolean getPasswordOld() {
        return passwordOld;
    }

    public void setPasswordOld(Boolean passwordOld) {
        this.passwordOld = passwordOld;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
