package com.example.attendee_university.model.dto.batch.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AddBatchMembersRequest(

        @NotEmpty(message = "At least one student id is required")
        List<UUID> studentIds
) {}