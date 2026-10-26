package com.oam.attendance.controller;

import com.oam.attendance.dto.*;
import com.oam.attendance.service.AttendanceService;
import com.oam.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/api/attendance/today")
    public ApiResponse<AttendanceTodayDto> today() {
        return ApiResponse.ok(attendanceService.today());
    }

    @PostMapping("/api/attendance/check-in")
    public ApiResponse<AttendanceRecordDto> checkIn(
            @RequestBody(required = false) AttendanceLocationRequest location) {
        return ApiResponse.ok("上班打卡成功", attendanceService.checkIn(location));
    }

    @PostMapping("/api/attendance/check-out")
    public ApiResponse<AttendanceRecordDto> checkOut(
            @RequestBody(required = false) AttendanceLocationRequest location) {
        return ApiResponse.ok("下班打卡成功", attendanceService.checkOut(location));
    }

    @GetMapping("/api/attendance/records")
    public ApiResponse<List<AttendanceRecordDto>> myRecords(@RequestParam(required = false) String month) {
        return ApiResponse.ok(attendanceService.myRecords(month));
    }

    @GetMapping("/api/attendance/stats")
    public ApiResponse<AttendanceStatsDto> myStats(@RequestParam(required = false) String month) {
        return ApiResponse.ok(attendanceService.myStats(month));
    }

    @GetMapping("/api/attendance/overview")
    public ApiResponse<List<AttendanceRecordDto>> overview(@RequestParam(required = false) String date) {
        return ApiResponse.ok(attendanceService.overview(date));
    }

    @PostMapping("/api/attendance/patches")
    public ApiResponse<AttendancePatchDto> submitPatch(@Valid @RequestBody AttendancePatchRequest request) {
        return ApiResponse.ok("补卡申请已提交", attendanceService.submitPatch(request));
    }

    @GetMapping("/api/attendance/patches")
    public ApiResponse<List<AttendancePatchDto>> listPatches() {
        return ApiResponse.ok(attendanceService.listPatches());
    }

    @GetMapping("/api/attendance/office-config")
    public ApiResponse<AttendanceOfficeConfigDto> officeConfig() {
        return ApiResponse.ok(attendanceService.getOfficeConfig());
    }

    @PutMapping("/api/attendance/office-config")
    public ApiResponse<AttendanceOfficeConfigDto> updateOfficeConfig(
            @RequestBody AttendanceOfficeConfigRequest request) {
        return ApiResponse.ok("打卡点配置已保存", attendanceService.saveOfficeConfig(request));
    }
}
