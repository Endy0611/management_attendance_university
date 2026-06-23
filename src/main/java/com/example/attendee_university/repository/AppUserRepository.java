package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    // used for login (email only in this case)
    Optional<AppUser> findByEmailIgnoreCase(String identifier);

    @Modifying
    @Query("UPDATE AppUser u SET u.verified = true, u.updatedAt = CURRENT_TIMESTAMP WHERE u.email = :email")
    void verifyUser(@Param("email") String email);

    @Modifying
    @Query("UPDATE AppUser u SET u.password = :password, u.updatedAt = CURRENT_TIMESTAMP WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("password") String password);
}