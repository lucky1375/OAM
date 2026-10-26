package com.oam.attendance.repository;

import com.oam.attendance.entity.AttendancePatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendancePatchRepository extends JpaRepository<AttendancePatch, Long> {

    List<AttendancePatch> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AttendancePatch> findByFlowId(Long flowId);
}
