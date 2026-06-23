package com.example.attendee_university.model.entity;

import com.example.attendee_university.model.constraint.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_users")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(name = "student_id", unique = true)
    private String studentId;

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "avatar")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role = RoleType.STUDENT;

    @ColumnDefault("false")
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @ColumnDefault("true")
    @Column(name = "is_first_login", nullable = false)
    private boolean firstLogin = true;

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ColumnDefault("false")
    @Column(name = "face_registered", nullable = false)
    private boolean faceRegistered = false;

    @ColumnDefault("false")
    @Column(name = "device_bound", nullable = false)
    private boolean deviceBound = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override public String getPassword()              { return password; }
    @Override public String getUsername()              { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return active; }

    public UUID getAppUserId() { return id; }

    public String getFirstName() {
        if (name == null || name.isBlank()) return "";
        String[] parts = name.trim().split("\\s+");
        return parts[0];
    }
}