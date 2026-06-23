package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.auth.request.AppUserRequest;
import com.example.attendee_university.model.dto.auth.request.AppUserUpdateRequest;
import com.example.attendee_university.model.dto.auth.request.ChangePasswordRequest;
import com.example.attendee_university.model.dto.auth.request.CreateUserRequest;
import com.example.attendee_university.model.dto.auth.response.AppUserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface AppUserService extends UserDetailsService {

    UserDetails loadUserByUsername(@NotBlank @NotNull String identifier);

    AppUserResponse register(@Valid AppUserRequest request);

    boolean getOtp(String email);

    void verifyEmailOtp(String email, String otp);

    void resendOtp(String email);

    void forgotPassword(String email);

    String verifyForgotPassword(String email, String otp);

    void resetPassword(String resetToken, String newPassword);

    AppUserResponse getMe();

    AppUserResponse updateMe(@Valid AppUserUpdateRequest request);

    void changePassword(@Valid ChangePasswordRequest request);

    void changeRole(UUID id, String role);

    void adminResetPassword(UUID id);

    AppUserResponse createUser(@Valid CreateUserRequest request);

    List<AppUserResponse> getAllUsers();
}