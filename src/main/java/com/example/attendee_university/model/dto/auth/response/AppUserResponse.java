package com.example.attendee_university.model.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponse {
    private UUID          id;
    private String        name;
    private String        email;
    private String        phone;
    private String        studentId;
    private Integer       generation;
    private String        role;
    private String        avatar;
    private boolean       verified;
    private boolean       active;
    private boolean       deviceBound;
    private boolean       firstLogin;
    private LocalDateTime createdAt;
}