package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.RoleType;
import com.example.attendee_university.model.dto.auth.request.AdminUpdateUserRequest;
import com.example.attendee_university.model.dto.auth.request.AppUserRequest;
import com.example.attendee_university.model.dto.auth.request.AppUserUpdateRequest;
import com.example.attendee_university.model.dto.auth.request.ChangePasswordRequest;
import com.example.attendee_university.model.dto.auth.request.CreateUserRequest;
import com.example.attendee_university.model.dto.auth.response.AppUserResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.DeviceFingerprint;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.DeviceFingerprintRepository;
import com.example.attendee_university.service.AppUserService;
import com.example.attendee_university.service.OtpService;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository           appUserRepository;
    private final DeviceFingerprintRepository deviceFingerprintRepository;
    private final PasswordEncoder             passwordEncoder;
    private final OtpService                  otpService;
    private final CompromisedPasswordChecker  compromisedPasswordChecker;
    private final HandleCurrentUser           handleCurrentUser;

    // ── Load user for Spring Security ─────────────────────────
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
    }

    // ── Register ──────────────────────────────────────────────
    @Override
    @Transactional
    public AppUserResponse register(AppUserRequest request) {
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(request.password());
        if (decision.isCompromised()) {
            throw new BadRequestException("Please choose a stronger password.");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        AppUser user = new AppUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());

        AppUser saved = appUserRepository.save(user);
        sendOtp(request.email());
        return toResponse(saved);
    }

    // ── Verify Email OTP ──────────────────────────────────────
    @Override
    @Transactional
    public void verifyEmailOtp(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            throw new BadRequestException("Email or OTP cannot be empty.");
        }

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email is not registered."));

        if (user.isVerified()) {
            throw new BadRequestException("Account is already verified.");
        }
        if (!otpService.verifyOtp(email, otp)) {
            throw new BadRequestException("OTP is invalid or has expired.");
        }

        appUserRepository.verifyUser(email);
        otpService.sendWelcome(email, user.getFirstName());
        log.info("Email verified for: {}", email);
    }

    // ── Resend OTP ────────────────────────────────────────────
    @Override
    public void resendOtp(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email is not registered."));

        if (user.isVerified()) {
            throw new BadRequestException("Account is already verified.");
        }
        sendOtp(email);
    }

    // ── Forgot Password ───────────────────────────────────────
    @Override
    public void forgotPassword(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email is not registered."));

        if (!user.isVerified()) throw new BadRequestException("Account is not verified.");
        if (!user.isActive())   throw new BadRequestException("Account is suspended.");

        sendOtp(email);
        log.info("Forgot password OTP sent to: {}", email);
    }

    // ── Verify Forgot Password OTP ────────────────────────────
    @Override
    public String verifyForgotPassword(String email, String otp) {
        appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email is not registered."));

        if (!otpService.verifyOtp(email, otp)) {
            throw new BadRequestException("OTP is invalid or has expired.");
        }

        String resetToken = otpService.generateResetToken(email);
        log.info("Reset token generated for: {}", email);
        return resetToken;
    }

    // ── Reset Password ────────────────────────────────────────
    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(newPassword);
        if (decision.isCompromised()) {
            throw new BadRequestException("Please choose a stronger password.");
        }

        String email = otpService.validateResetToken(resetToken);
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found."));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from the old password.");
        }

        appUserRepository.updatePassword(email, passwordEncoder.encode(newPassword));
        log.info("Password reset for: {}", email);
    }

    // ── Get current user profile ──────────────────────────────
    @Override
    public AppUserResponse getMe() {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));
        return toResponse(user);
    }

    // ── Update current user profile ───────────────────────────
    @Override
    @Transactional
    public AppUserResponse updateMe(AppUserUpdateRequest request) {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));

        user.setName(request.name());
        user.setPhone(request.phone());
        user.setAvatar(request.avatar());
        return toResponse(user);
    }

    // ── Deactivate own account ────────────────────────────────
    @Override
    @Transactional
    public void deactivateMe() {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));
        user.setActive(false);
        log.info("User deactivated their own account: {}", user.getEmail());
    }

    // ── Change password ───────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password.");
        }

        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(request.newPassword());
        if (decision.isCompromised()) {
            throw new BadRequestException("Please choose a stronger password.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setFirstLogin(false);
    }

    // ── Helpers ───────────────────────────────────────────────
    @Override
    public boolean getOtp(String email) {
        return sendOtp(email);
    }

    private boolean sendOtp(String email) {
        try {
            String otp = otpService.generateOtp();
            otpService.sendOtp(email, otp);
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", email, e.getMessage());
            return false;
        }
    }

    // ── ADMIN: Create user ────────────────────────────────────
    @Override
    @Transactional
    public AppUserResponse createUser(CreateUserRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        String tempPassword = otpService.generateOtp();

        AppUser user = new AppUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStudentId(request.studentId());
        user.setGeneration(request.generation());
        user.setRole(request.role());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setVerified(true);
        user.setFirstLogin(true);

        AppUser saved = appUserRepository.save(user);
        otpService.sendWelcome(request.email(), user.getFirstName());
        log.info("User created by admin: {}", request.email());
        return toResponse(saved);
    }

    // ── ADMIN: List all users ─────────────────────────────────
    @Override
    public List<AppUserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ── ADMIN: Get single user ────────────────────────────────
    @Override
    public AppUserResponse getUserById(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return toResponse(user);
    }

    // ── ADMIN: Update user info ───────────────────────────────
    @Override
    @Transactional
    public AppUserResponse adminUpdateUser(UUID id, AdminUpdateUserRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.setName(request.name());
        user.setPhone(request.phone());
        user.setStudentId(request.studentId());
        user.setGeneration(request.generation());
        user.setAvatar(request.avatar());
        log.info("Admin updated user: {}", user.getEmail());
        return toResponse(user);
    }

    // ── ADMIN: Change role ────────────────────────────────────
    @Override
    @Transactional
    public void changeRole(UUID id, String role) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        RoleType newRole;
        try {
            newRole = RoleType.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role);
        }

        user.setRole(newRole);
        log.info("Role changed to {} for user: {}", newRole, user.getEmail());
    }

    // ── ADMIN: Reset password ─────────────────────────────────
    @Override
    @Transactional
    public void adminResetPassword(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        String tempPassword = otpService.generateOtp();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setFirstLogin(true);

        otpService.sendWelcome(user.getEmail(), user.getFirstName());
        log.info("Password reset by admin for: {}", user.getEmail());
    }

    // ── ADMIN: Ban / unban user ───────────────────────────────
    @Override
    @Transactional
    public void setUserActive(UUID id, boolean active) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
        user.setActive(active);
        log.info("User {} set active={} by admin", user.getEmail(), active);
    }

    // ── ADMIN: Reset device binding ───────────────────────────
    @Override
    @Transactional
    public void adminResetDevice(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        deviceFingerprintRepository.findByAppUserId(id).ifPresent(device -> {
            deviceFingerprintRepository.delete(device);
            log.info("Device fingerprint deleted for user: {}", user.getEmail());
        });

        user.setDeviceBound(false);
        log.info("Device binding reset by admin for: {}", user.getEmail());
    }

    // ── toResponse ────────────────────────────────────────────
    private AppUserResponse toResponse(AppUser user) {
        return AppUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .studentId(user.getStudentId())
                .generation(user.getGeneration())
                .role(user.getRole().name())
                .avatar(user.getAvatar())
                .verified(user.isVerified())
                .active(user.isActive())
                .deviceBound(user.isDeviceBound())
                .firstLogin(user.isFirstLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}