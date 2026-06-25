package com.example.attendee_university.model.dto.file.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileMetaDataResponse {
    String fileName;
    String fileType;
    String fileUrl;
    Long fileSize;
}

