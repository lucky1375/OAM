package com.oam.meeting.repository;

import com.oam.meeting.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByMeetingId(Long meetingId);

    void deleteByMeetingId(Long meetingId);
}
