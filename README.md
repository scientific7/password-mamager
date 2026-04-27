# 🔐 SecureVault — Password Manager

A comprehensive, enterprise-grade **Password Manager** web application built with **Spring Boot 4.0**, **Thymeleaf**, **Spring Security**, and **MySQL**. Designed with a security-first approach to help users securely store, manage, and audit their credentials.

---

## 👨‍💻 Team Members

| # | Name | SAP ID |
|---|------|--------|
| 1 | Ankit Aman | 590018956 |
| 2 | Nitin Baghel | 590018953 |
| 3 | Amar Sangamnath Devkatte | 590018914 |
| 4 | Mohd. Zaid | 590017505 |

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Security Architecture](#-security-architecture)
- [Application Pages](#-application-pages)
- [Database Schema](#-database-schema)
- [Build Status](#-build-status)

---

## ✨ Features

### 🔑 Authentication & Account Security
- **User Registration** with mandatory email verification
- **Login Lockout** — account locked after 5 failed attempts (15-min window)
- **Multi-Factor Authentication (MFA)** — TOTP-based with QR code setup (Google Authenticator, Authy, etc.)
- **Secure Password Reset** — email-based, SHA-256 hashed tokens, 30-min expiry
- **Session Security** — HttpOnly + SameSite=Strict cookies, 30-min timeout
- **Session Fixation Protection** — session ID migrated on every login

### 🗄️ Password Vault
- Store credentials: title, website URL, username, encrypted password, encrypted notes
- Mark entries as **Favorites**
- Organize entries into custom **Categories**
- Track **last accessed time** and **access count** per entry
- Full **CRUD** — create, view, edit, delete vault entries
- **Backup & Export** of vault data

### 🔒 Encryption
- All passwords and notes encrypted at rest using **AES encryption** via `EncryptionService`
- **Password fingerprinting** for duplicate detection without decryption

### 🛡️ Password Health Analysis
- Detect **weak passwords** (length, entropy, pattern analysis)
- Detect **duplicate passwords** across entries (via fingerprints)
- Identify **old/stale passwords** based on last update timestamp
- **Health score** summary on dashboard

### 🔢 Password Generator
- Configurable length and character sets (uppercase, lowercase, digits, special chars)
- Cryptographically secure random generation
- One-click copy to clipboard

### 📋 Audit Logging
- Logs all security-sensitive events: logins, vault changes, password resets, MFA changes
- Timestamped entries with user context
- Dedicated audit log viewer

### 📧 Email Notifications
- Branded **HTML email templates** for verification and password reset
- Powered by Spring Mail + Thymeleaf rendering

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 4.0.5 |
| Language | Java 25 |
| Frontend / Templating | Thymeleaf + Thymeleaf Spring Security Extras |
| Security | Spring Security 6 |
| ORM / Persistence | Spring Data JPA / Hibernate |
| Database (Production) | MySQL |
| Database (Testing) | H2 (in-memory) |
| Email | Spring Boot Starter Mail |
| QR Code Generation | Google ZXing 3.5.1 |
| Validation | Jakarta Bean Validation |
| Build Tool | Apache Maven |

---

## 📁 Project Structure

```
src/main/java/com/example/passwordmanager/
├── PasswordManagerApplication.java       # Main entry point
│
├── config/
│   ├── PasswordEncoderConfig.java        # BCrypt bean
│   └── WebConfig.java                    # Web MVC configuration
│
├── security/
│   ├── SecurityConfig.java               # Spring Security filter chain
│   ├── CustomUserDetailsService.java     # UserDetails loader
│   ├── UserPrincipal.java                # Lockout & verification checks
│   ├── MfaAuthenticationSuccessHandler.java  # Redirects to MFA if enabled
│   ├── MfaVerificationFilter.java        # Enforces MFA completion
│   └── LoginFailureHandler.java          # Tracks failed attempts & lockout
│
├── entity/
│   ├── User.java                         # User account with security fields
│   ├── VaultEntry.java                   # Encrypted credential record
│   ├── Category.java                     # Vault organization category
│   └── AuditLog.java                     # Security event log
│
├── repository/
│   ├── UserRepository.java
│   ├── VaultEntryRepository.java
│   ├── CategoryRepository.java
│   └── AuditLogRepository.java
│
├── service/
│   ├── UserService.java                  # Registration, verification, lockout
│   ├── VaultService.java                 # Vault CRUD operations
│   ├── EncryptionService.java            # AES encrypt/decrypt
│   ├── PasswordGeneratorService.java     # Secure password generation
│   ├── PasswordHealthService.java        # Health scoring & duplicate detection
│   ├── CategoryService.java
│   ├── AuditService.java                 # Event logging
│   ├── EmailService.java                 # HTML email sending
│   ├── OtpService.java                   # TOTP verification
│   └── CustomUserDetailsService.java
│
├── controller/
│   ├── AuthController.java               # /register, /verify-email
│   ├── PasswordRecoveryController.java   # /forgot-password, /reset-password
│   ├── MfaController.java               # /mfa/**
│   ├── VaultController.java              # /vault/**
│   ├── CategoryController.java           # /categories/**
│   ├── DashboardController.java          # /dashboard
│   ├── GeneratorController.java          # /generator
│   ├── AuditController.java              # /audit/**
│   ├── ProfileController.java            # /profile/**
│   ├── BackupController.java             # /backup/**
│   ├── SecurityController.java
│   └── HomeController.java               # / (landing & redirects)
│
├── dto/                                  # Form binding / API data transfer objects
│   ├── LoginDto.java
│   ├── RegisterDto.java
│   ├── VaultEntryDto.java
│   ├── CategoryDto.java
│   ├── PasswordGeneratorDto.java
│   ├── PasswordHealthDto.java
│   ├── ChangePasswordDto.java
│   ├── ProfileDto.java
│   ├── SecurityAuditDto.java
│   ├── VaultExportDto.java
│   └── BackupExportDto.java
│
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── DuplicateEmailException.java
    └── UnauthorizedAccessException.java

src/main/resources/
├── application.properties
└── templates/
    ├── auth/
    │   ├── login.html
    │   ├── register.html
    │   ├── verify-email.html
    │   ├── forgot-password.html
    │   ├── reset-password.html
    │   └── mfa.html
    ├── dashboard/index.html
    ├── vault/
    │   ├── list.html
    │   └── edit-entry.html
    ├── category/categories.html
    ├── generator/index.html
    ├── audit/logs.html
    ├── profile/settings.html
    ├── email/
    │   ├── verify-email.html
    │   └── password-reset.html
    ├── layout/base.html
    ├── fragments/messages.html
    └── error/
        ├── 400.html
        ├── 403.html
        ├── 404.html
        └── 500.html
```

---

## ✅ Prerequisites

Make sure the following are installed before running the project:

- **Java 25** (JDK)
- **Apache Maven 3.8+**
- **MySQL 8.0+**
- An **SMTP email account** (Gmail, Outlook, etc.) for email features

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/password-manager.git
cd password-manager
```

### 2. Create the MySQL Database

```sql
CREATE DATABASE password_manager;
```

### 3. Configure the Application

Edit `src/main/resources/application.properties` — see the [Configuration](#-configuration) section below.

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The app starts on a **random port** (configured as `server.port=0`). Check the console output for the actual port:

```
Started PasswordManagerApplication in X.XXXs (... on port XXXXX)
```

Then open your browser at:

```
http://localhost:<PORT>
```

---

## ⚙️ Configuration

All configuration lives in `src/main/resources/application.properties`.

### Database

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/password_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### Email / SMTP

```properties
spring.mail.host=smtp.gmail.com         # Replace with your SMTP host
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=your-email@gmail.com
```

> **Tip for Gmail:** Enable 2FA on your Google account and generate an **App Password** to use here instead of your real password.

### Encryption & Security

```properties
app.encryption.secret=YourStrongSecretKey   # Change this in production!
app.verification.expiry-minutes=60           # Email verification link expiry
```

### Password Generator Defaults

```properties
app.password.min-length=8
app.password.default-length=12
```

### Session

```properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=Strict
```

---

## 🔐 Security Architecture

### Authentication Flow

```
User Login
    │
    ├─► Credentials checked (BCrypt hash comparison)
    │       ├─► Failed? → Increment counter → Lock after 5 attempts (15 min)
    │       └─► Success? → Reset counter
    │
    ├─► Email verified? → If not, redirect to verification page
    │
    ├─► MFA enabled? → If yes, redirect to OTP entry page
    │
    └─► Grant access → Authenticated session created
```

### Token Security

| Token Type | Hashing | Expiry | Storage |
|-----------|---------|--------|---------|
| Email Verification | SHA-256 | 60 minutes | Hash only (never plain text) |
| Password Reset | SHA-256 | 30 minutes | Hash only (never plain text) |

### Session Security

- **HttpOnly** cookies — JavaScript cannot access the session cookie
- **SameSite=Strict** — prevents cross-site request forgery attacks
- **Session fixation protection** — new session ID issued on every login
- **30-minute timeout** — idle sessions automatically invalidated

### Data Encryption

- All vault passwords and notes are **AES-encrypted** before being written to the database
- **Password fingerprints** (one-way hashes) stored alongside ciphertext for duplicate detection without ever decrypting

---

## 📄 Application Pages

| Page | URL | Description |
|------|-----|-------------|
| Home | `/` | Landing page / redirect |
| Login | `/login` | Sign in with optional MFA |
| Register | `/register` | Create new account |
| Verify Email | `/verify-email` | Email link confirmation |
| MFA Setup | `/mfa/setup` | QR code generation for authenticator app |
| MFA Verify | `/mfa/verify` | OTP entry on login |
| Forgot Password | `/forgot-password` | Request password reset email |
| Reset Password | `/reset-password` | Set new password via token |
| Dashboard | `/dashboard` | Vault overview & health summary |
| Vault | `/vault` | List all credentials |
| Add Entry | `/vault/new` | Create new vault entry |
| Edit Entry | `/vault/edit/{id}` | Modify existing entry |
| Categories | `/categories` | Manage vault categories |
| Generator | `/generator` | Standalone password generator |
| Audit Logs | `/audit/logs` | View security event history |
| Profile Settings | `/profile/settings` | Update name, password, MFA |
| Backup | `/backup` | Export vault data |

---

## 🗃️ Database Schema

### `users`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated ID |
| `full_name` | VARCHAR | User's display name |
| `email` | VARCHAR (UNIQUE) | Login email |
| `password_hash` | VARCHAR | BCrypt hashed password |
| `role` | VARCHAR | Default: `ROLE_USER` |
| `mfa_enabled` | BOOLEAN | MFA toggle flag |
| `mfa_secret` | VARCHAR | TOTP shared secret |
| `email_verified` | BOOLEAN | Email confirmation flag |
| `email_verification_token_hash` | VARCHAR | SHA-256 hashed token |
| `email_verification_expires_at` | TIMESTAMP | Verification link expiry |
| `password_reset_token_hash` | VARCHAR | SHA-256 hashed reset token |
| `password_reset_expires_at` | TIMESTAMP | Reset link expiry |
| `failed_login_attempts` | INT | Failed attempt counter |
| `lockout_expires_at` | TIMESTAMP | Account lockout expiry |
| `created_at` / `updated_at` | TIMESTAMP | Audit timestamps |

### `vault_entries`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated ID |
| `user_id` | BIGINT (FK) | Owner user |
| `category_id` | BIGINT (FK) | Assigned category |
| `title` | VARCHAR | Entry label |
| `website_url` | VARCHAR | Associated URL |
| `username` | VARCHAR | Login username |
| `encrypted_password` | VARCHAR | AES-encrypted password |
| `password_fingerprint` | VARCHAR | Hash for duplicate detection |
| `encrypted_notes` | LONGTEXT | AES-encrypted notes |
| `favorite` | BOOLEAN | Favorite flag |
| `last_accessed_at` | TIMESTAMP | Last view timestamp |
| `access_count` | INT | Total accesses |
| `created_at` / `updated_at` | TIMESTAMP | Audit timestamps |

### `categories`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated ID |
| `user_id` | BIGINT (FK) | Owner user |
| `name` | VARCHAR | Category label |
| `created_at` | TIMESTAMP | Creation timestamp |

### `audit_logs`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated ID |
| `user_id` | BIGINT (FK) | Acting user |
| `action` | VARCHAR | Event type (e.g., LOGIN, VAULT_CREATE) |
| `details` | TEXT | Additional context |
| `ip_address` | VARCHAR | Client IP |
| `timestamp` | TIMESTAMP | Event time |

---

## 🏗️ Build Status

| Metric | Status |
|--------|--------|
| Maven Build | ✅ SUCCESS |
| Source Files Compiled | ✅ All 57 files |
| Application Startup | ✅ Confirmed |
| Database Schema Migration | ✅ Complete |
| Email Templates | ✅ Rendering correctly |
| MFA / QR Code | ✅ Working |
| Session Security | ✅ Configured |

---

## 🔮 Roadmap

- [ ] Browser extension (Chrome / Firefox) for auto-fill
- [ ] HaveIBeenPwned breach monitoring integration
- [ ] Secure credential sharing between users
- [ ] Native iOS & Android mobile apps
- [ ] Family / Organization plans with role-based access
- [ ] Passkey / WebAuthn passwordless authentication
- [ ] Self-hosting via Docker
- [ ] Developer REST API
- [ ] Dark web monitoring alerts

---

## 📜 License

This project is developed for academic purposes by the team members listed above.

---

> **Security Notice:** Before deploying to production, replace all placeholder secrets in `application.properties` with strong, randomly generated values. Never commit real credentials to version control.
