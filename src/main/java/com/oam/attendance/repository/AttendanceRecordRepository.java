package com.oam.attendance.repository;

import com.oam.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    List<AttendanceRecord> findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(Long userId, LocalDate start, LocalDate end);

    List<AttendanceRecord> findByWorkDateOrderByUserNameAsc(LocalDate workDate);

    List<AttendanceRecord> findByWorkDateBetweenOrderByWorkDateDesc(LocalDate start, LocalDate end);
}
