package com.example.passwordmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BackupExportDto {

    private LocalDateTime exportedAt;
    private int itemCount;
    private List<VaultExportDto> entries;

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public List<VaultExportDto> getEntries() {
        return entries;
    }

    public void setEntries(List<VaultExportDto> entries) {
        this.entries = entries;
    }
}
