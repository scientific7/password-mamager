# Password Manager Design Documentation

## 1) Use Case Diagram

```mermaid
flowchart LR
    User[User]

    subgraph "Password Manager"
        Register((Register))
        VerifyEmail((Verify Email))
        Login((Login))
        MFA((Multi-Factor Authentication))
        ResetPassword((Reset Password))
        ViewDashboard((View Dashboard))
        ManageVaultEntries((Manage Vault Entries))
        ManageCategories((Manage Categories))
        GeneratePassword((Generate Password))
        UpdateProfile((Update Profile))
        ViewAuditLogs((View Audit Logs))
    end

    User --> Register
    User --> VerifyEmail
    User --> Login
    User --> MFA
    User --> ResetPassword
    User --> ViewDashboard
    User --> ManageVaultEntries
    User --> ManageCategories
    User --> GeneratePassword
    User --> UpdateProfile
    User --> ViewAuditLogs
```

---

## 2) Sequence Diagram — Login

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant AuthController
    participant SecurityFilter as UsernamePasswordAuthenticationFilter
    participant CustomUserDetailsService
    participant UserRepository
    participant Database
    participant MfaHandler as MfaAuthenticationSuccessHandler
    participant MfaPage as MfaController

    User ->> Browser: open /login
    Browser ->> AuthController: submit credentials
    AuthController ->> SecurityFilter: forward login request
    SecurityFilter ->> CustomUserDetailsService: loadUserByUsername(email)
    CustomUserDetailsService ->> UserRepository: findByEmail(email)
    UserRepository ->> Database: SELECT user
    Database -->> UserRepository: user record
    UserRepository -->> CustomUserDetailsService: UserPrincipal
    CustomUserDetailsService -->> SecurityFilter: authentication success
    SecurityFilter ->> MfaHandler: onAuthenticationSuccess
    MfaHandler ->> UserRepository: check MFA enabled
    alt MFA enabled
        MfaHandler ->> Browser: redirect /mfa
        Browser ->> MfaPage: submit OTP
        MfaPage ->> UserService: validate OTP
        MfaPage -->> Browser: dashboard
    else MFA disabled
        MfaHandler -->> Browser: redirect /dashboard
    end
```

---

## 3) Sequence Diagram — Registration

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant AuthController
    participant UserService
    participant UserRepository
    participant PasswordEncoder
    participant EmailService
    participant EmailServer
    participant Database

    User ->> Browser: open /register
    Browser ->> AuthController: POST /register with RegisterDto
    AuthController ->> UserService: registerUser(dto, appUrl)
    UserService ->> UserRepository: findByEmail(email)
    UserRepository ->> Database: SELECT email
    Database -->> UserRepository: none
    UserService ->> PasswordEncoder: encode(password)
    UserService ->> UserRepository: save(User)
    UserRepository ->> Database: INSERT user
    UserRepository -->> UserService: saved user
    UserService ->> EmailService: sendVerificationEmail(user, token, appUrl)
    EmailService ->> EmailServer: send verification email
    EmailServer -->> User: receives email
    User ->> Browser: click verify link
    Browser ->> PasswordRecoveryController: GET /verify-email?token=...
    PasswordRecoveryController ->> UserService: verifyEmailToken(token)
    UserService ->> UserRepository: findByEmailVerificationTokenHash(hash)
    UserRepository ->> Database: SELECT user
    Database -->> UserRepository: user record
    UserService -->> PasswordRecoveryController: verification result
    PasswordRecoveryController -->> Browser: show success page
```

---

## 4) Class Diagram

```mermaid
classDiagram
    class AuthController {
        +loginPage()
        +showRegisterForm()
        +handleRegister()
    }
    class PasswordRecoveryController {
        +forgotPasswordPage()
        +requestPasswordReset()
        +resetPasswordPage()
        +resetPassword()
        +verifyEmail()
    }
    class UserService {
        +registerUser()
        +getCurrentUser()
        +updateProfile()
        +changePassword()
        +requestPasswordReset()
        +resetPassword()
        +verifyEmailToken()
        +enableMfa()
        +disableMfa()
    }
    class CustomUserDetailsService {
        +loadUserByUsername()
    }
    class SecurityConfig
    class MfaAuthenticationSuccessHandler
    class MfaVerificationFilter
    class EmailService
    class PasswordEncoder
    class UserRepository
    class User {
        +id
        +fullName
        +email
        +passwordHash
        +role
        +mfaEnabled
        +emailVerified
        +createdAt
        +updatedAt
    }
    class RegisterDto
    class LoginDto
    class ProfileDto
    class VaultEntryDto
    class CategoryDto

    AuthController --> UserService
    PasswordRecoveryController --> UserService
    PasswordRecoveryController --> EmailService
    UserService --> UserRepository
    UserService --> EmailService
    UserService --> PasswordEncoder
    CustomUserDetailsService --> UserRepository
    MfaAuthenticationSuccessHandler --> UserRepository
    SecurityConfig --> MfaAuthenticationSuccessHandler
    SecurityConfig --> MfaVerificationFilter
    UserRepository --> User
    AuthController --> RegisterDto
    PasswordRecoveryController --> LoginDto
    UserService --> ProfileDto
```

---

## 5) Activity Diagram — User Workflow

```mermaid
flowchart TD
    Start([Start]) --> LoginRegister{User has account?}
    LoginRegister -->|No| Register[Register account]
    Register --> VerifyEmail[Verify email]
    VerifyEmail --> Login[Login]
    LoginRegister -->|Yes| Login
    Login --> Credentials{Credentials valid?}
    Credentials -->|No| LoginFail[Show error]
    LoginFail --> Login
    Credentials -->|Yes| MFA{MFA enabled?}
    MFA -->|Yes| ShowMfa[MFA challenge]
    MFA -->|No| Dashboard[Open dashboard]
    ShowMfa -->|Success| Dashboard
    ShowMfa -->|Fail| LoginFail
    Dashboard --> Vault[Manage vault entries]
    Dashboard --> Categories[Manage categories]
    Dashboard --> Generate[Generate password]
    Dashboard --> Profile[Update profile]
    Dashboard --> Audit[View audit logs]
    Dashboard --> Logout[Logout]
    Logout --> End([End])
```

---

## 6) Component Diagram

```mermaid
graph TB
    Browser[User Browser]
    WebApp[Web UI (Thymeleaf)]
    Controllers[Controllers]
    Services[Service Layer]
    Security[Security Layer]
    Repositories[Repository Layer]
    Database[MySQL Database]
    EmailServer[SMTP / Email Service]

    Browser --> WebApp
    WebApp --> Controllers
    Controllers --> Services
    Services --> Repositories
    Repositories --> Database
    Services --> EmailServer
    Security --> Controllers
    Security --> CustomUserDetailsService
    Security --> MfaVerificationFilter
    Security --> MfaAuthenticationSuccessHandler
    CustomUserDetailsService --> Repositories
    Services --> PasswordEncoder[Password Encoder]
```

---

## 7) Deployment Diagram

```mermaid
graph LR
    Browser[User Browser]
    AppServer[Spring Boot App\n(Java 26, Spring Boot 4.0.5)]
    MySQL[MySQL Database]
    SMTP[SMTP / Email Server]

    Browser -->|HTTPS / HTTP| AppServer
    AppServer -->|JDBC| MySQL
    AppServer -->|SMTP| SMTP
    AppServer -->|Session / Cookies| Browser
```
