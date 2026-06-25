package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.session.request.GroupSessionRequest;
import com.example.attendee_university.model.dto.session.response.GroupSessionResponse;

import java.util.List;
import java.util.UUID;

public interface GroupSessionService {

    GroupSessionResponse createSession(GroupSessionRequest request);

    List<GroupSessionResponse> getAllSessions();

    List<GroupSessionResponse> getSessionsByGroup(UUID groupId);

    GroupSessionResponse getSessionById(UUID id);

    GroupSessionResponse updateSession(UUID id, GroupSessionRequest request);

    void deleteSession(UUID id);

    List<GroupSessionResponse> getMyActiveSessions();

    List<GroupSessionResponse> getPastSessionsForMyGroups();
}