package com.example.attendee_university.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private HttpStatus status;
    private T payload;
    private Instant timestamp;

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> response =
                ApiResponse.<T>builder()
                        .success(true)
                        .message("Success")
                        .status(HttpStatus.OK)
                        .payload(data)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        ApiResponse<T> response =
                ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .status(HttpStatus.OK)
                        .payload(data)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        ApiResponse<T> response =
                ApiResponse.<T>builder()
                        .success(false)
                        .message(message)
                        .status(status)
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        ApiResponse<T> response =
                ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .status(HttpStatus.CREATED)
                        .payload(data)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .status(HttpStatus.OK)
                .payload(null)
                .timestamp(Instant.now())
                .build();
    }
}