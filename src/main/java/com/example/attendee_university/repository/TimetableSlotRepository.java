package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.TimetableSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    List<TimetableSlot> findByGroupId(UUID groupId);

    List<TimetableSlot> findByGroupIdOrderByDayOfWeekAscStartTimeAsc(UUID groupId);

    // batched lookup for "my weekly timetable" — avoids one query per group
    List<TimetableSlot> findByGroupIdIn(List<UUID> groupIds);

    // room conflict lookup: everything already booked in this zone on this day
    List<TimetableSlot> findByZoneIdAndDayOfWeek(UUID zoneId, DayOfWeek dayOfWeek);

    // batch conflict lookup: everything this same group already has on this day
    List<TimetableSlot> findByGroupIdAndDayOfWeek(UUID groupId, DayOfWeek dayOfWeek);

    // instructor conflict lookup: everything booked on this day across all groups this instructor teaches
    List<TimetableSlot> findByGroupIdInAndDayOfWeek(List<UUID> groupIds, DayOfWeek dayOfWeek);

    // locking variant of findByZoneIdAndDayOfWeek used during conflict-checking, so two concurrent
    // create/update calls for the same room+day can't both pass the check before either commits
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TimetableSlot s WHERE s.zoneId = :zoneId AND s.dayOfWeek = :dayOfWeek")
    List<TimetableSlot> findByZoneIdAndDayOfWeekForUpdate(@Param("zoneId") UUID zoneId,
                                                          @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
