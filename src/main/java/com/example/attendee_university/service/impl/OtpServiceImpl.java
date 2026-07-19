package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final long   OTP_TTL_SECONDS         = 600;
    private static final long   RESET_TOKEN_TTL_SECONDS = 900;
    private static final String OTP_PREFIX              = "otp:";
    private static final String RESET_TOKEN_PREFIX      = "reset:";
    private static final SecureRandom SECURE_RANDOM     = new SecureRandom();

    private final JavaMailSender          mailSender;
    private final SpringTemplateEngine    templateEngine;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.from}")
    private String mailFrom;

    @Override
    public String generateOtp() {
        // SecureRandom instead of Math.random() — OTPs are a security control,
        // Math.random()'s PRNG is predictable and not safe for this.
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void sendOtp(String email, String otp) {
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiryMinutes", OTP_TTL_SECONDS / 60);
        String html = templateEngine.process("otp-email", context);

        sendEmail(email, "Verify your email with OTP", html);
        log.info("OTP sent to: {}", email);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String key       = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean isOtpPresent(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(OTP_PREFIX + email));
    }

    @Override
    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + token, email, RESET_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public String validateResetToken(String token) {
        String key   = RESET_TOKEN_PREFIX + token;
        String email = redisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new BadRequestException("Invalid or expired reset token.");
        }

        redisTemplate.delete(key);
        return email;
    }

    @Override
    public void sendWelcome(String email, String firstName) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        String html = templateEngine.process("welcome-email", context);
        sendEmail(email, "Welcome! 🎉", html);
        log.info("Welcome email sent to: {}", email);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email.", e);
        }
    }
}