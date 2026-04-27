package com.example.passwordmanager.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.passwordmanager.dto.BackupExportDto;
import com.example.passwordmanager.dto.PasswordHealthDto;
import com.example.passwordmanager.dto.SecurityAuditDto;
import com.example.passwordmanager.dto.VaultEntryDto;
import com.example.passwordmanager.dto.VaultExportDto;
import com.example.passwordmanager.entity.Category;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.entity.VaultEntry;
import com.example.passwordmanager.exception.ResourceNotFoundException;
import com.example.passwordmanager.exception.UnauthorizedAccessException;
import com.example.passwordmanager.repository.VaultEntryRepository;

@Service
@Transactional(readOnly = true)
public class VaultService {

    private final VaultEntryRepository vaultEntryRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EncryptionService encryptionService;
    private final PasswordHealthService passwordHealthService;

    public VaultService(VaultEntryRepository vaultEntryRepository,
                        UserService userService,
                        CategoryService categoryService,
                        EncryptionService encryptionService,
                        PasswordHealthService passwordHealthService) {
        this.vaultEntryRepository = vaultEntryRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.encryptionService = encryptionService;
        this.passwordHealthService = passwordHealthService;
    }

    public List<VaultEntry> listEntriesForUser(Long userId, String query) {
        if (query != null && !query.trim().isEmpty()) {
            return vaultEntryRepository.findByUserIdAndTitleContainingIgnoreCase(userId, query.trim());
        }
        return vaultEntryRepository.findByUserId(userId);
    }

    public List<VaultEntryDto> listEntryDtosForCurrentUser(String query) {
        return listEntryDtosForCurrentUser(query, null, null);
    }

    public List<VaultEntryDto> listEntryDtosForCurrentUser(String query, Long categoryId, String filter) {
        User user = userService.getCurrentUser();
        List<VaultEntry> entries = listEntriesForUser(user.getId(), query).stream()
                .filter(entry -> categoryId == null
                        || (entry.getCategory() != null && categoryId.equals(entry.getCategory().getId())))
                .toList();
        Map<String, Long> fingerprintCounts = fingerprintCounts(entries);

        return entries.stream()
                .map(entry -> toListDto(entry, fingerprintCounts))
                .filter(dto -> matchesFilter(dto, filter))
                .collect(Collectors.toList());
    }

    @Transactional
    public VaultEntryDto getEntryDtoForView(Long id) {
        User user = userService.getCurrentUser();
        VaultEntry entry = getOwnedEntry(id, user);
        Map<String, Long> fingerprintCounts = fingerprintCounts(listEntriesForUser(user.getId(), null));
        String fingerprint = fingerprintForEntry(entry);
        boolean reused = fingerprint != null && fingerprintCounts.getOrDefault(fingerprint, 0L) > 1;
        entry.setLastAccessedAt(LocalDateTime.now());
        entry.setAccessCount(entry.getAccessCount() == null ? 1 : entry.getAccessCount() + 1);

        return toDetailDto(entry, reused);
    }

    public VaultEntryDto getEntryDtoForEdit(Long id) {
        User user = userService.getCurrentUser();
        VaultEntry entry = getOwnedEntry(id, user);
        Map<String, Long> fingerprintCounts = fingerprintCounts(listEntriesForUser(user.getId(), null));
        String fingerprint = fingerprintForEntry(entry);
        boolean reused = fingerprint != null && fingerprintCounts.getOrDefault(fingerprint, 0L) > 1;
        return toDetailDto(entry, reused);
    }

    @Transactional
    public Long createEntryForCurrentUser(VaultEntryDto dto) {
        User user = userService.getCurrentUser();
        Category category = categoryService.findEntityById(dto.getCategoryId());

        VaultEntry entry = new VaultEntry();
        entry.setUser(user);
        entry.setCategory(category);
        entry.setTitle(dto.getTitle());
        entry.setWebsiteUrl(dto.getWebsiteUrl());
        entry.setUsername(dto.getUsername());
        entry.setEncryptedPassword(encryptionService.encrypt(dto.getPassword()));
        entry.setPasswordFingerprint(passwordHealthService.fingerprint(dto.getPassword()));
        entry.setEncryptedNotes(dto.getNotes() != null && !dto.getNotes().isBlank()
                ? encryptionService.encrypt(dto.getNotes()) : null);
        entry.setFavorite(Boolean.TRUE.equals(dto.getFavorite()));
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        return vaultEntryRepository.save(entry).getId();
    }

    @Transactional
    public void updateEntryForCurrentUser(Long id, VaultEntryDto dto) {
        User user = userService.getCurrentUser();
        VaultEntry entry = getOwnedEntry(id, user);

        Category category = categoryService.findEntityById(dto.getCategoryId());

        entry.setCategory(category);
        entry.setTitle(dto.getTitle());
        entry.setWebsiteUrl(dto.getWebsiteUrl());
        entry.setUsername(dto.getUsername());
        entry.setEncryptedPassword(encryptionService.encrypt(dto.getPassword()));
        entry.setPasswordFingerprint(passwordHealthService.fingerprint(dto.getPassword()));
        entry.setEncryptedNotes(dto.getNotes() != null && !dto.getNotes().isBlank()
                ? encryptionService.encrypt(dto.getNotes()) : null);
        entry.setFavorite(Boolean.TRUE.equals(dto.getFavorite()));
        entry.setUpdatedAt(LocalDateTime.now());

        vaultEntryRepository.save(entry);
    }

    @Transactional
    public void deleteEntryForCurrentUser(Long id) {
        User user = userService.getCurrentUser();
        VaultEntry entry = getOwnedEntry(id, user);

        vaultEntryRepository.delete(entry);
    }

    public List<VaultEntryDto> listRecentDtosForCurrentUser(int limit) {
        User user = userService.getCurrentUser();
        Map<String, Long> fingerprintCounts = fingerprintCounts(listEntriesForUser(user.getId(), null));
        return listEntriesForUser(user.getId(), null).stream()
                .filter(entry -> entry.getLastAccessedAt() != null)
                .sorted(Comparator.comparing(VaultEntry::getLastAccessedAt).reversed())
                .limit(limit)
                .map(entry -> toListDto(entry, fingerprintCounts))
                .toList();
    }

    public List<VaultEntryDto> listMostUsedDtosForCurrentUser(int limit) {
        User user = userService.getCurrentUser();
        Map<String, Long> fingerprintCounts = fingerprintCounts(listEntriesForUser(user.getId(), null));
        return listEntriesForUser(user.getId(), null).stream()
                .filter(entry -> entry.getAccessCount() != null && entry.getAccessCount() > 0)
                .sorted(Comparator.comparing((VaultEntry entry) -> entry.getAccessCount() == null ? 0 : entry.getAccessCount())
                        .reversed())
                .limit(limit)
                .map(entry -> toListDto(entry, fingerprintCounts))
                .toList();
    }

    public SecurityAuditDto buildSecurityAuditForCurrentUser() {
        User user = userService.getCurrentUser();
        List<VaultEntry> entries = listEntriesForUser(user.getId(), null);
        Map<String, Long> fingerprintCounts = fingerprintCounts(entries);
        List<VaultEntryDto> dtos = entries.stream()
                .map(entry -> toListDto(entry, fingerprintCounts))
                .toList();

        SecurityAuditDto audit = new SecurityAuditDto();
        audit.setTotalEntries(dtos.size());
        audit.setFavoriteCount((int) dtos.stream().filter(entry -> Boolean.TRUE.equals(entry.getFavorite())).count());
        audit.setWeakEntries(dtos.stream()
                .filter(entry -> "Weak".equals(entry.getPasswordStrengthLabel()))
                .toList());
        audit.setReusedEntries(dtos.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getPasswordReused()))
                .toList());
        audit.setOldEntries(dtos.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getPasswordOld()))
                .toList());
        audit.setWeakCount(audit.getWeakEntries().size());
        audit.setReusedCount(audit.getReusedEntries().size());
        audit.setOldCount(audit.getOldEntries().size());
        audit.setHealthScore(calculateHealthScore(audit));
        audit.setHealthLabel(audit.getHealthScore() >= 80 ? "Good" : audit.getHealthScore() >= 55 ? "Medium" : "Needs attention");
        return audit;
    }

    public BackupExportDto exportVaultForCurrentUser() {
        User user = userService.getCurrentUser();
        List<VaultExportDto> exports = listEntriesForUser(user.getId(), null).stream()
                .map(this::toExportDto)
                .toList();

        BackupExportDto backup = new BackupExportDto();
        backup.setExportedAt(LocalDateTime.now());
        backup.setItemCount(exports.size());
        backup.setEntries(exports);
        return backup;
    }

    private VaultEntry getOwnedEntry(Long id, User user) {
        VaultEntry entry = vaultEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Unauthorized access");
        }

        return entry;
    }

    private VaultEntryDto toListDto(VaultEntry entry, Map<String, Long> fingerprintCounts) {
        VaultEntryDto dto = new VaultEntryDto();
        dto.setId(entry.getId());
        dto.setTitle(entry.getTitle());
        dto.setWebsiteUrl(entry.getWebsiteUrl());
        dto.setUsername(entry.getUsername());
        dto.setFavorite(entry.getFavorite());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        dto.setLastAccessedAt(entry.getLastAccessedAt());
        dto.setAccessCount(entry.getAccessCount());
        dto.setCategoryName(entry.getCategory() != null ? entry.getCategory().getName() : null);
        String fingerprint = fingerprintForEntry(entry);
        String password = encryptionService.decrypt(entry.getEncryptedPassword());
        boolean reused = fingerprint != null && fingerprintCounts.getOrDefault(fingerprint, 0L) > 1;
        applyHealth(dto, password, reused);
        return dto;
    }

    private VaultEntryDto toDetailDto(VaultEntry entry, boolean reused) {
        VaultEntryDto dto = new VaultEntryDto();
        dto.setId(entry.getId());
        dto.setTitle(entry.getTitle());
        dto.setWebsiteUrl(entry.getWebsiteUrl());
        dto.setUsername(entry.getUsername());
        String password = encryptionService.decrypt(entry.getEncryptedPassword());
        dto.setPassword(password);
        dto.setNotes(entry.getEncryptedNotes() != null ? encryptionService.decrypt(entry.getEncryptedNotes()) : null);
        dto.setFavorite(entry.getFavorite());
        dto.setCategoryId(entry.getCategory() != null ? entry.getCategory().getId() : null);
        dto.setCategoryName(entry.getCategory() != null ? entry.getCategory().getName() : null);
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        dto.setLastAccessedAt(entry.getLastAccessedAt());
        dto.setAccessCount(entry.getAccessCount());
        applyHealth(dto, password, reused);
        return dto;
    }

    private VaultExportDto toExportDto(VaultEntry entry) {
        VaultExportDto dto = new VaultExportDto();
        dto.setTitle(entry.getTitle());
        dto.setWebsiteUrl(entry.getWebsiteUrl());
        dto.setUsername(entry.getUsername());
        dto.setPassword(encryptionService.decrypt(entry.getEncryptedPassword()));
        dto.setNotes(entry.getEncryptedNotes() != null ? encryptionService.decrypt(entry.getEncryptedNotes()) : null);
        dto.setCategoryName(entry.getCategory() != null ? entry.getCategory().getName() : null);
        dto.setFavorite(entry.getFavorite());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        return dto;
    }

    private void applyHealth(VaultEntryDto dto, String password, boolean reused) {
        PasswordHealthDto health = passwordHealthService.analyze(password, reused, dto.getUpdatedAt());
        dto.setPasswordStrengthLabel(health.getLabel());
        dto.setPasswordStrengthScore(health.getScore());
        dto.setPasswordReused(health.isReused());
        dto.setPasswordOld(health.isOld());
    }

    private Map<String, Long> fingerprintCounts(List<VaultEntry> entries) {
        return entries.stream()
                .map(this::fingerprintForEntry)
                .filter(fingerprint -> fingerprint != null && !fingerprint.isBlank())
                .collect(Collectors.groupingBy(fingerprint -> fingerprint, Collectors.counting()));
    }

    private String fingerprintForEntry(VaultEntry entry) {
        if (entry.getPasswordFingerprint() != null && !entry.getPasswordFingerprint().isBlank()) {
            return entry.getPasswordFingerprint();
        }
        return passwordHealthService.fingerprint(encryptionService.decrypt(entry.getEncryptedPassword()));
    }

    private boolean matchesFilter(VaultEntryDto dto, String filter) {
        if (filter == null || filter.isBlank() || "all".equalsIgnoreCase(filter)) {
            return true;
        }
        return switch (filter.toLowerCase()) {
            case "weak" -> "Weak".equals(dto.getPasswordStrengthLabel());
            case "reused" -> Boolean.TRUE.equals(dto.getPasswordReused());
            case "old" -> Boolean.TRUE.equals(dto.getPasswordOld());
            case "favorites" -> Boolean.TRUE.equals(dto.getFavorite());
            default -> true;
        };
    }

    private int calculateHealthScore(SecurityAuditDto audit) {
        if (audit.getTotalEntries() == 0) {
            return 100;
        }
        int deductions = audit.getWeakCount() * 18 + audit.getReusedCount() * 22 + audit.getOldCount() * 8;
        return Math.max(0, 100 - Math.min(100, deductions));
    }
}
