package com.example.attendee_university.controller;


import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.file.response.FileMetaDataResponse;
import com.example.attendee_university.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> uploadFile(
            @Valid @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) {
        FileMetaDataResponse fileMetaData = fileService.uploadFile(file, folder);
        ApiResponse<FileMetaDataResponse> response = ApiResponse.<FileMetaDataResponse>builder()
                .success(true)
                .message("File upload successfully to RustFS")
                .status(HttpStatus.CREATED)
                .payload(fileMetaData)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/preview-file")
    public ResponseEntity<Resource> previewFile(@RequestParam String key) {
        Resource resource = fileService.getFileByFileName(key);
        String contentType = MediaTypeFactory.getMediaType(key)
                .map(MediaType::toString)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam String key) {
        fileService.deleteFile(key);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("File deleted successfully.")
                .status(HttpStatus.OK)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}