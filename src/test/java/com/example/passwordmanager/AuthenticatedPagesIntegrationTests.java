package com.example.passwordmanager;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;

import com.example.passwordmanager.entity.AuditLog;
import com.example.passwordmanager.entity.Category;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.entity.VaultEntry;
import com.example.passwordmanager.repository.AuditLogRepository;
import com.example.passwordmanager.repository.CategoryRepository;
import com.example.passwordmanager.repository.UserRepository;
import com.example.passwordmanager.repository.VaultEntryRepository;
import com.example.passwordmanager.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthenticatedPagesIntegrationTests {

    private static final String EMAIL = "viewer@example.com";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VaultEntryRepository vaultEntryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EncryptionService encryptionService;

    private Long entryId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        auditLogRepository.deleteAll();
        vaultEntryRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setFullName("Viewer User");
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole("ROLE_USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Category category = new Category();
        category.setUser(user);
        category.setName("Work");
        category.setColor("blue");
        category = categoryRepository.save(category);

        VaultEntry entry = new VaultEntry();
        entry.setUser(user);
        entry.setCategory(category);
        entry.setTitle("Mail");
        entry.setWebsiteUrl("https://mail.example.com");
        entry.setUsername("viewer");
        entry.setEncryptedPassword(encryptionService.encrypt("secret123"));
        entry.setFavorite(true);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entryId = vaultEntryRepository.save(entry).getId();

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction("TEST_ACTION");
        log.setTargetType("VAULT_ENTRY");
        log.setTargetId(entryId);
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Test
    void authenticatedPagesRenderWithVaultData() throws Exception {
        mockMvc.perform(get("/dashboard").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));

        mockMvc.perform(get("/vault/{id}", entryId).with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("vault/view-entry"));

        mockMvc.perform(get("/vault/{id}/edit", entryId).with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("vault/edit-entry"));

        mockMvc.perform(get("/categories").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("category/categories"));

        mockMvc.perform(get("/audit").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("audit/logs"));

        mockMvc.perform(get("/profile/settings").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/settings"));

        mockMvc.perform(get("/generator").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("generator/index"));

        mockMvc.perform(get("/security").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("security/index"));
    }

    @Test
    void authenticatedUserCanExportVaultBackup() throws Exception {
        mockMvc.perform(get("/backup/export").with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk());
    }
}
