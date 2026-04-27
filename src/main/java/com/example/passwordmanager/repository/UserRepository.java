package com.example.passwordmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.passwordmanager.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPasswordResetTokenHash(String tokenHash);
    Optional<User> findByEmailVerificationTokenHash(String tokenHash);
}