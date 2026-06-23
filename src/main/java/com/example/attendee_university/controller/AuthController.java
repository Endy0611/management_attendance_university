package com.example.attendee_university.controller;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.jwt.JwtService;
import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.auth.request.*;
import com.example.attendee_university.model.dto.auth.response.AppUserResponse;
import com.example.attendee_university.model.dto.auth.response.AuthResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.service.AppUserService;
import com.example.attendee_university.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // ── Private helper — renamed to avoid recursion ───────────
    private void doAuthenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new BadRequestException("Account is disabled.");
        } catch (BadCredentialsException e) {
            throw new BadRequestException(
                    "Invalid email or password. Please check your credentials and try again.");
        }
    }

    // ── Login ─────────────────────────────────────────────────
    @Operation(summary = "User Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid AuthRequest request,
            HttpServletRequest httpRequest) {

        doAuthenticate(request.identifier(), request.password());

        final UserDetails userDetails = appUserService.loadUserByUsername(request.identifier());
        AppUser user = (AppUser) userDetails;

        if (!user.isVerified()) {
            throw new BadRequestException("Account is not verified. Please verify your email first.");
        }

        boolean rememberMe = request.rememberMe() != null && request.rememberMe();

        final String accessToken  = jwtService.generateToken(userDetails);
        final String refreshToken = refreshTokenService.issueToken(
                user.getAppUserId(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr(),
                rememberMe
        );

        return new ResponseEntity<>(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successful!")
                        .status(HttpStatus.OK)
                        .payload(new AuthResponse(accessToken, refreshToken, user.isFirstLogin()))
                        .timestamp(Instant.now())
                        .build(),
                HttpStatus.OK
        );
    }

    // ── Register ──────────────────────────────────────────────
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AppUserResponse>> register(
            @RequestBody @Valid AppUserRequest request) {

        AppUserResponse response = appUserService.register(request);

        return new ResponseEntity<>(
                ApiResponse.<AppUserResponse>builder()
                        .success(true)
                        .message("Registered successfully! Please verify your email.")
                        .status(HttpStatus.CREATED)
                        .payload(response)
                        .timestamp(Instant.now())
                        .build(),
                HttpStatus.CREATED
        );
    }

    // ── Verify OTP ────────────────────────────────────────────
    @Operation(summary = "Verify email with OTP")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        appUserService.verifyEmailOtp(email, otp);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Email verified! You can now log in.")
                        .status(HttpStatus.OK)
                        .timestamp(Instant.now())
                        .build()
        );
    }

    // ── Resend OTP ────────────────────────────────────────────
    @Operation(summary = "Resend verification OTP")
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resend(@RequestParam String email) {
        appUserService.resendOtp(email);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("OTP resent to your email.")
                        .status(HttpStatus.OK)
                        .timestamp(Instant.now())
                        .build()
        );
    }

    // ── Forgot Password ───────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        appUserService.forgotPassword(request.email());
        return ApiResponse.success("OTP sent to your email.", null);
    }

    // ── Verify Forgot Password OTP ────────────────────────────
    @PostMapping("/verify-forgot-password")
    public ResponseEntity<ApiResponse<String>> verifyForgotPassword(
            @RequestBody @Valid VerifyForgotPasswordRequest request) {
        String resetToken = appUserService.verifyForgotPassword(request.email(), request.otp());
        return ApiResponse.success("OTP verified.", resetToken);
    }

    // ── Reset Password ────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        appUserService.resetPassword(request.resetToken(), request.newPassword());
        return ApiResponse.success("Password reset successfully.", null);
    }

    // ── Change Password (logged-in user, e.g. first login) ────
    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        appUserService.changePassword(request);
        return ApiResponse.success("Password changed successfully.", null);
    }

    // ── Get current user ──────────────────────────────────────
    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AppUserResponse>> getMe() {
        return ApiResponse.success("User fetched.", appUserService.getMe());
    }

    // ── Update current user ───────────────────────────────────
    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AppUserResponse>> updateMe(
            @RequestBody @Valid AppUserUpdateRequest request) {
        return ApiResponse.success("User updated.", appUserService.updateMe(request));
    }

    // ── Refresh token ─────────────────────────────────────────
    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody @Valid RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse authResponse = refreshTokenService.rotate(request, httpRequest);
        return ApiResponse.success("Token refreshed successfully.", authResponse);
    }

    // ── Logout ────────────────────────────────────────────────
    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody @Valid RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request);
        return ApiResponse.success("Logged out successfully.", null);
    }
}