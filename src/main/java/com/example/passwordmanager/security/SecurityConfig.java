package com.example.passwordmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MfaAuthenticationSuccessHandler mfaAuthenticationSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final MfaVerificationFilter mfaVerificationFilter;

    public SecurityConfig(MfaAuthenticationSuccessHandler mfaAuthenticationSuccessHandler,
                          LoginFailureHandler loginFailureHandler,
                          MfaVerificationFilter mfaVerificationFilter) {
        this.mfaAuthenticationSuccessHandler = mfaAuthenticationSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.mfaVerificationFilter = mfaVerificationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/forgot-password", "/reset-password", "/verify-email", "/css/**", "/js/**", "/images/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(mfaAuthenticationSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation(config -> config.migrateSession())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .addFilterAfter(mfaVerificationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
