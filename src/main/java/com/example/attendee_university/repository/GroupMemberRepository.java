package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByGroupId(UUID groupId);

    List<GroupMember> findByAppUserId(UUID appUserId);

    Optional<GroupMember> findByGroupIdAndAppUserId(UUID groupId, UUID appUserId);

    boolean existsByGroupIdAndAppUserId(UUID groupId, UUID appUserId);

    long countByGroupId(UUID groupId);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.groupId = :groupId AND m.appUserId = :appUserId")
    void deleteByGroupIdAndAppUserId(@Param("groupId") UUID groupId, @Param("appUserId") UUID appUserId);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.groupId = :groupId")
    void deleteAllByGroupId(@Param("groupId") UUID groupId);
}