package com.oam.meeting.repository;

import com.oam.meeting.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}