package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.group.request.AddGroupMembersRequest;
import com.example.attendee_university.model.dto.group.request.GroupRequest;
import com.example.attendee_university.model.dto.group.response.GroupMemberResponse;
import com.example.attendee_university.model.dto.group.response.GroupResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    GroupResponse createGroup(GroupRequest request);

    List<GroupResponse> getAllGroups();

    GroupResponse getGroupById(UUID id);

    GroupResponse updateGroup(UUID id, GroupRequest request);

    void deleteGroup(UUID id);

    // groups for the current logged-in user (role-aware: student->enrolled, instructor->taught, admin->all)
    List<GroupResponse> getMyGroups();

    List<GroupMemberResponse> addMembers(UUID groupId, AddGroupMembersRequest request);

    void removeMember(UUID groupId, UUID studentId);

    List<GroupMemberResponse> getMembers(UUID groupId);
}