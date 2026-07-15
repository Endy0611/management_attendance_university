package com.example.attendee_university.websocket.handler;

import com.example.attendee_university.model.dto.websocket.NotificationEvent;
import com.example.attendee_university.model.dto.websocket.SessionStatusEvent;
import com.example.attendee_university.model.entity.Group;
import com.example.attendee_university.model.entity.GroupMember;
import com.example.attendee_university.model.entity.GroupSession;
import com.example.attendee_university.model.entity.Zone;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.GroupMemberRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.repository.GroupSessionRepository;
import com.example.attendee_university.repository.ZoneRepository;
import com.example.attendee_university.model.constraint.NotificationType;
import com.example.attendee_university.model.constraint.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionSchedular {

    private static final String OPENED_KEY = "session:opened:";
    private static final String CLOSED_KEY = "session:closed:";

    private final GroupSessionRepository  groupSessionRepository;
    private final GroupRepository         groupRepository;
    private final ZoneRepository          zoneRepository;
    private final GroupMemberRepository   groupMemberRepository;
    private final AppUserRepository       appUserRepository;
    private final WebSocketPublisher      wsPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedDelay = 60_000)
    public void checkSessions() {
        Instant now = Instant.now();
        // Only pull sessions that could plausibly need an OPENED/CLOSED
        // transition right now — not the entire history of every session
        // ever created. Anything that ended more than a day ago is long
        // past both the OPENED (2h) and CLOSED (24h) Redis dedup TTLs below,
        // so it can never fire again and doesn't need to be loaded.
        List<GroupSession> candidates = groupSessionRepository.findRelevantForScheduler(now.minus(Duration.ofHours(24)));

        for (GroupSession session : candidates) {
            boolean active  = session.isActive(now);
            boolean expired = session.isExpired(now);

            if (active && !alreadySent(OPENED_KEY, session)) {
                broadcastOpened(session);
                markSent(OPENED_KEY, session, Duration.ofHours(2));
            }

            if (expired && !alreadySent(CLOSED_KEY, session)) {
                broadcastClosed(session);
                markSent(CLOSED_KEY, session, Duration.ofHours(24));
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────
    private void broadcastOpened(GroupSession session) {
        SessionStatusEvent event = buildStatusEvent(session, SessionStatus.OPENED);
        wsPublisher.publishSessionStatus(session.getId(), event);

        // Notify each enrolled student
        notifyStudents(session, NotificationType.SESSION_STARTING,
                "Session started",
                "Your session in " + zoneName(session) + " is now open for check-in.");

        log.info("[Scheduler] Session OPENED: {}", session.getId());
    }

    private void broadcastClosed(GroupSession session) {
        SessionStatusEvent event = buildStatusEvent(session, SessionStatus.CLOSED);
        wsPublisher.publishSessionStatus(session.getId(), event);

        notifyStudents(session, NotificationType.SESSION_ENDED,
                "Session ended",
                "The session has ended. Check-in is now closed.");

        log.info("[Scheduler] Session CLOSED: {}", session.getId());
    }

    private void notifyStudents(GroupSession session, NotificationType type, String title, String message) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(session.getGroupId());
        for (GroupMember member : members) {
            appUserRepository.findById(member.getAppUserId()).ifPresent(student ->
                    wsPublisher.notifyUser(student.getEmail(), NotificationEvent.builder()
                            .type(type)
                            .title(title)
                            .message(message)
                            .timestamp(Instant.now())
                            .build()));
        }
    }

    private SessionStatusEvent buildStatusEvent(GroupSession session, SessionStatus status) {
        Group group = groupRepository.findById(session.getGroupId()).orElse(null);
        Zone zone   = zoneRepository.findById(session.getZoneId()).orElse(null);

        return SessionStatusEvent.builder()
                .sessionId(session.getId())
                .groupName(group != null ? group.getName() : null)
                .zoneName(zone  != null ? zone.getName()  : null)
                .status(status)
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .eventTime(Instant.now())
                .build();
    }

    private String zoneName(GroupSession session) {
        return zoneRepository.findById(session.getZoneId())
                .map(Zone::getName)
                .orElse("the designated zone");
    }

    private boolean alreadySent(String prefix, GroupSession session) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(prefix + session.getId()));
    }

    private void markSent(String prefix, GroupSession session, Duration ttl) {
        redisTemplate.opsForValue().set(prefix + session.getId(), "1", ttl);
    }
}