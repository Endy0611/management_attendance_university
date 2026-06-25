package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.file.response.FileMetaDataResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileMetaDataResponse uploadFile(MultipartFile file, String folder);

    Resource getFileByFileName(String fileName);

    void deleteFile(String fileName);
}