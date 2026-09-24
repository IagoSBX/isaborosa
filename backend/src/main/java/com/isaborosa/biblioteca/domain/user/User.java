package com.isaborosa.biblioteca.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(String name, String username, String passwordHash, boolean mustChangePassword) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.mustChangePassword = mustChangePassword;
        this.active = true;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.mustChangePassword = false;
    }

    /** Usado so pelo seeder da senha inicial: define o hash e mantem a troca obrigatoria. */
    public void initializePassword(String initialPasswordHash) {
        this.passwordHash = initialPasswordHash;
        this.mustChangePassword = true;
    }

    public void recordLogin(Instant when) {
        this.lastLogin = when;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
