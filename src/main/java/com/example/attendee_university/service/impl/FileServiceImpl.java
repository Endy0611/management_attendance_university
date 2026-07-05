package com.example.attendee_university.service.impl;


import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.file.response.FileMetaDataResponse;
import com.example.attendee_university.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;

    @Value("${rustfs.bucket.name}")
    private String bucketName;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "zip", "pdf", "docx", "png", "jpg", "jpeg", "mov", "mp4","svg", "webp"
    );

    public void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    @Override
    public FileMetaDataResponse uploadFile(MultipartFile file, String folder) {
        createBucketIfNotExists();

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (ObjectUtils.isEmpty(extension) || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed! Supported formats: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        String fileName = UUID.randomUUID() + "." + extension;
        String key = folder + "/" + fileName;
        String contentType = file.getContentType() != null
                ? file.getContentType() : "application/octet-stream";

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to RustFS", e);
        }

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/files/preview-file")
                .queryParam("key", key)
                .toUriString();

        return new FileMetaDataResponse(key, contentType, fileUrl, file.getSize());
    }

    @SneakyThrows
    @Override
    public Resource getFileByFileName(String key) {
        if (ObjectUtils.isEmpty(key)) {
            throw new BadRequestException("File name is required.");
        }
        try {
            InputStream inputStream =
                    s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());
            return new InputStreamResource(inputStream);
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("File not found: " + key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from RustFS: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        if (ObjectUtils.isEmpty(fileName)) {
            throw new BadRequestException("File name is required.");
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName).key(fileName).build());
            log.info("File deleted: {}", fileName);
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("File not found: " + fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }
}