package com.example.passwordmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.passwordmanager.entity.VaultEntry;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
    List<VaultEntry> findByUserId(Long userId);
    List<VaultEntry> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
}