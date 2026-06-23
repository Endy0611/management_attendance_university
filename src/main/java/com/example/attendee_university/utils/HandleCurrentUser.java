package com.example.attendee_university.utils;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandleCurrentUser {

    private final AppUserRepository appUserRepository;

    private AppUser resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("Please login to continue.");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof AppUser appUser) {
            return appUser;
        }

        if (principal instanceof String emailOrAnon) {
            if ("anonymousUser".equals(emailOrAnon)) {
                throw new BadRequestException("Please login to continue.");
            }
            // ← changed: getUserByEmailOrUsername → findByEmail (JPA)
            return appUserRepository.findByEmail(emailOrAnon)
                    .orElseThrow(() -> new BadRequestException("User not found: " + emailOrAnon));
        }

        throw new BadRequestException("Invalid authentication state.");
    }

    public UUID getUserIdOfCurrentUser() {
        return resolveCurrentUser().getAppUserId();
    }

    public AppUser getCurrentUser() {
        return resolveCurrentUser();
    }

    public String getUserByEmail() {
        return resolveCurrentUser().getEmail();
    }

    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(auth) || !auth.isAuthenticated()) return "";

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
    }
}