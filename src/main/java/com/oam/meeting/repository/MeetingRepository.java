package com.oam.meeting.repository;

import com.oam.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByMeetingRoomId(Long meetingRoomId);

    /** 某会议室在 [start,end) 之间已有的会议（供冲突检测） */
    List<Meeting> findByMeetingRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(Long meetingRoomId,
                                                                               LocalDateTime endTime,
                                                                               LocalDateTime startTime);
}