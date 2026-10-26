package com.oam.attendance.repository;

import com.oam.attendance.entity.AttendanceOfficeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceOfficeConfigRepository extends JpaRepository<AttendanceOfficeConfig, Long> {
}
