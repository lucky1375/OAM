package com.oam.calendar.repository;

import com.oam.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByOwnerUserId(Long ownerUserId);

    List<CalendarEvent> findBySourceMeetingId(Long sourceMeetingId);

    List<CalendarEvent> findBySourceFlowId(Long sourceFlowId);

    List<CalendarEvent> findBySourceAttendanceId(Long sourceAttendanceId);
}