package com.example.attendee_university.service;

public interface OtpService {
    String generateOtp();

    void sendOtp(String email, String otp);

    boolean verifyOtp(String email, String otp);

    boolean isOtpPresent(String email);

    String generateResetToken(String email);

    String validateResetToken(String resetToken);

    void sendWelcome(String email, String firstName);
}
