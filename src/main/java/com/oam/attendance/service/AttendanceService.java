package com.oam.attendance.service;

import com.oam.attendance.dto.*;
import com.oam.attendance.entity.AttendanceOfficeConfig;
import com.oam.attendance.entity.AttendancePatch;
import com.oam.attendance.entity.AttendanceRecord;
import com.oam.attendance.repository.AttendanceOfficeConfigRepository;
import com.oam.attendance.repository.AttendancePatchRepository;
import com.oam.attendance.repository.AttendanceRecordRepository;
import com.oam.calendar.entity.CalendarEvent;
import com.oam.calendar.repository.CalendarRepository;
import com.oam.common.security.SecurityUtil;
import com.oam.user.entity.Role;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import com.oam.workflow.dto.ApprovalFlowDto;
import com.oam.workflow.dto.ApprovalSubmitRequest;
import com.oam.workflow.event.ApprovalFinishedEvent;
import com.oam.workflow.service.ApprovalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    public static final LocalTime WORK_START = LocalTime.of(9, 0);
    public static final LocalTime WORK_END = LocalTime.of(18, 0);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AttendanceRecordRepository recordRepository;
    private final AttendancePatchRepository patchRepository;
    private final AttendanceOfficeConfigRepository officeConfigRepository;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;
    private final CalendarRepository calendarRepository;

    @Value("${oam.attendance.office.name:公司总部}")
    private String officeName;

    @Value("${oam.attendance.office.latitude:39.908823}")
    private double officeLatitude;

    @Value("${oam.attendance.office.longitude:116.397470}")
    private double officeLongitude;

    @Value("${oam.attendance.office.radius:500}")
    private int officeRadius;

    @Value("${oam.baidu-map.ak:}")
    private String baiduAk;

    public AttendanceService(AttendanceRecordRepository recordRepository,
                             AttendancePatchRepository patchRepository,
                             AttendanceOfficeConfigRepository officeConfigRepository,
                             UserRepository userRepository,
                             ApprovalService approvalService,
                             CalendarRepository calendarRepository) {
        this.recordRepository = recordRepository;
        this.patchRepository = patchRepository;
        this.officeConfigRepository = officeConfigRepository;
        this.userRepository = userRepository;
        this.approvalService = approvalService;
        this.calendarRepository = calendarRepository;
    }

    /** 首次启动时用 application.yml 中的默认值初始化打卡点配置 */
    @PostConstruct
    void initOfficeConfig() {
        if (officeConfigRepository.count() > 0) return;
        AttendanceOfficeConfig config = new AttendanceOfficeConfig();
        config.setOfficeName(officeName);
        config.setLatitude(officeLatitude);
        config.setLongitude(officeLongitude);
        config.setRadiusMeters(officeRadius);
        config.setUpdatedBy("system");
        officeConfigRepository.save(config);
    }

    // ==================== 打卡 ====================

    @Transactional(readOnly = true)
    public AttendanceTodayDto today() {
        User user = requireUser();
        LocalDate today = LocalDate.now();
        AttendanceRecord record = recordRepository.findByUserIdAndWorkDate(user.getId(), today).orElse(null);
        AttendanceTodayDto dto = new AttendanceTodayDto();
        dto.setDate(today.format(DATE_FMT));
        dto.setServerTime(LocalDateTime.now().format(CLOCK_FMT));
        dto.setWorkStart(WORK_START.format(TIME_FMT));
        dto.setWorkEnd(WORK_END.format(TIME_FMT));
        dto.setRecord(record == null ? null : AttendanceRecordDto.from(record));
        dto.setCanCheckIn(record == null || record.getCheckInTime() == null);
        dto.setCanCheckOut(record != null && record.getCheckInTime() != null
                && record.getCheckOutTime() == null);
        AttendanceOfficeConfig config = currentOfficeConfig();
        dto.setOfficeName(config.getOfficeName());
        dto.setOfficeLatitude(config.getLatitude());
        dto.setOfficeLongitude(config.getLongitude());
        dto.setRadiusMeters(config.getRadiusMeters());
        dto.setBaiduAk(baiduAk == null ? "" : baiduAk);
        return dto;
    }

    @Transactional
    public AttendanceRecordDto checkIn(AttendanceLocationRequest location) {
        User user = requireUser();
        String address = validateLocation(location);
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = recordRepository.findByUserIdAndWorkDate(user.getId(), today).orElse(null);
        if (record == null) {
            record = newRecord(user, today);
        } else if (record.getCheckInTime() != null) {
            throw new IllegalArgumentException("今天已打过上班卡");
        }
        record.setCheckInTime(now);
        record.setCheckInStatus(now.toLocalTime().isAfter(WORK_START) ? "LATE" : "NORMAL");
        record.setCheckInAddress(address);
        record = recordRepository.save(record);
        syncPunchToCalendar(record);
        return AttendanceRecordDto.from(record);
    }

    @Transactional
    public AttendanceRecordDto checkOut(AttendanceLocationRequest location) {
        User user = requireUser();
        String address = validateLocation(location);
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = recordRepository.findByUserIdAndWorkDate(user.getId(), today)
                .orElseThrow(() -> new IllegalArgumentException("请先打上班卡"));
        if (record.getCheckInTime() == null) {
            throw new IllegalArgumentException("请先打上班卡");
        }
        if (record.getCheckOutTime() != null) {
            throw new IllegalArgumentException("今天已打过下班卡");
        }
        if (now.isBefore(record.getCheckInTime())) {
            throw new IllegalArgumentException("下班时间不能早于上班时间");
        }
        record.setCheckOutTime(now);
        record.setCheckOutStatus(now.toLocalTime().isBefore(WORK_END) ? "EARLY" : "NORMAL");
        record.setWorkMinutes((int) Duration.between(record.getCheckInTime(), now).toMinutes());
        record.setCheckOutAddress(address);
        record = recordRepository.save(record);
        syncPunchToCalendar(record);
        return AttendanceRecordDto.from(record);
    }

    /** 校验打卡位置是否在允许范围内，返回用于展示的地址描述 */
    private String validateLocation(AttendanceLocationRequest location) {
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            throw new IllegalArgumentException("请允许获取定位后再打卡");
        }
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("定位坐标不合法");
        }
        AttendanceOfficeConfig config = currentOfficeConfig();
        long distance = Math.round(distanceMeters(lat, lng, config.getLatitude(), config.getLongitude()));
        if (distance > config.getRadiusMeters()) {
            throw new IllegalArgumentException("当前不在打卡范围内，距「" + config.getOfficeName() + "」约 "
                    + distance + " 米（允许 " + config.getRadiusMeters() + " 米）");
        }
        String address = location.getAddress();
        if (address == null || address.trim().isEmpty()) {
            address = String.format("%.6f,%.6f", lat, lng);
        }
        return address.trim();
    }

    /** Haversine 距离（米） */
    private double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 打卡时间同步到个人日历，一天一条，下班打卡时更新为完整区间 */
    private void syncPunchToCalendar(AttendanceRecord record) {
        if (record.getCheckInTime() == null) return;
        List<CalendarEvent> events = calendarRepository.findBySourceAttendanceId(record.getId());
        CalendarEvent event = events.isEmpty() ? new CalendarEvent() : events.get(0);
        event.setOwnerUserId(record.getUserId());
        event.setAllDay(false);
        event.setLocation(currentOfficeConfig().getOfficeName());
        event.setRemark("考勤打卡");
        event.setSourceAttendanceId(record.getId());
        if (record.getCheckOutTime() != null) {
            event.setTitle("考勤打卡 " + record.getCheckInTime().format(TIME_FMT)
                    + "-" + record.getCheckOutTime().format(TIME_FMT));
            event.setStartTime(record.getCheckInTime());
            LocalDateTime end = record.getCheckOutTime();
            event.setEndTime(end.isAfter(record.getCheckInTime()) ? end : record.getCheckInTime().plusMinutes(1));
        } else {
            event.setTitle("上班打卡 " + record.getCheckInTime().format(TIME_FMT));
            event.setStartTime(record.getCheckInTime());
            event.setEndTime(record.getCheckInTime().plusMinutes(30));
        }
        if (event.getId() == null) {
            event.setCreatedBy(SecurityUtil.currentUsername());
        }
        event.setUpdatedBy(SecurityUtil.currentUsername());
        calendarRepository.save(event);
    }

    // ==================== 记录与统计 ====================

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> myRecords(String month) {
        User user = requireUser();
        YearMonth ym = parseMonth(month);
        return recordRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(
                        user.getId(), ym.atDay(1), ym.atEndOfMonth())
                .stream().map(AttendanceRecordDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceStatsDto myStats(String month) {
        User user = requireUser();
        YearMonth ym = parseMonth(month);
        List<AttendanceRecord> records = recordRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(
                user.getId(), ym.atDay(1), ym.atEndOfMonth());
        int late = 0, early = 0, total = 0;
        for (AttendanceRecord r : records) {
            if ("LATE".equals(r.getCheckInStatus())) late++;
            if ("EARLY".equals(r.getCheckOutStatus())) early++;
            if (r.getWorkMinutes() != null) total += r.getWorkMinutes();
        }
        AttendanceStatsDto dto = new AttendanceStatsDto();
        dto.setMonth(ym.toString());
        dto.setWorkDays(records.size());
        dto.setLateCount(late);
        dto.setEarlyCount(early);
        dto.setTotalMinutes(total);
        dto.setAvgDuration(records.isEmpty() ? "0小时" : AttendanceRecordDto.formatDuration(total / records.size()));
        return dto;
    }

    /** 全员某日考勤概览，仅管理员 */
    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> overview(String date) {
        requireAdmin();
        LocalDate day = parseDate(date, LocalDate.now());
        Map<Long, AttendanceRecord> recordMap = recordRepository.findByWorkDateOrderByUserNameAsc(day)
                .stream().collect(Collectors.toMap(AttendanceRecord::getUserId, Function.identity(), (a, b) -> a));
        List<AttendanceRecordDto> result = new ArrayList<>();
        for (User user : userRepository.findAllByOrderByCreatedAtAsc()) {
            if (Boolean.FALSE.equals(user.getEnabled())) continue;
            AttendanceRecord record = recordMap.get(user.getId());
            if (record != null) {
                result.add(AttendanceRecordDto.from(record));
            } else {
                AttendanceRecordDto empty = new AttendanceRecordDto();
                empty.setWorkDate(day.format(DATE_FMT));
                empty.setUserId(user.getId());
                empty.setUsername(user.getUsername());
                empty.setUserName(user.getNickname());
                empty.setChecked(false);
                result.add(empty);
            }
        }
        return result;
    }

    // ==================== 补卡（统一走流程审批） ====================

    @Transactional
    public AttendancePatchDto submitPatch(AttendancePatchRequest request) {
        User user = requireUser();
        LocalDate workDate = parseDate(request.getWorkDate(), null);
        if (workDate == null) {
            throw new IllegalArgumentException("补卡日期格式不正确");
        }
        if (workDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("不能为未来日期申请补卡");
        }
        String type = request.getPatchType().trim().toUpperCase();
        if (!"IN".equals(type) && !"OUT".equals(type)) {
            throw new IllegalArgumentException("补卡类型不正确");
        }
        LocalTime time;
        try {
            time = LocalTime.parse(request.getPatchTime().trim(), TIME_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException("补卡时间格式不正确，应为 HH:mm");
        }

        String typeName = "IN".equals(type) ? "上班卡" : "下班卡";
        ApprovalSubmitRequest submit = new ApprovalSubmitRequest();
        submit.setType("PATCH");
        submit.setTitle("补卡申请：" + workDate.format(DATE_FMT) + " " + typeName);
        submit.setContent("补卡时间 " + time.format(TIME_FMT) + "；补卡原因：" + request.getReason().trim());
        submit.setApproverUserId(request.getApproverUserId());
        ApprovalFlowDto flow = approvalService.submit(submit);

        AttendancePatch patch = new AttendancePatch();
        patch.setUserId(user.getId());
        patch.setUsername(user.getUsername());
        patch.setUserName(user.getNickname());
        patch.setWorkDate(workDate);
        patch.setPatchType(type);
        patch.setPatchTime(LocalDateTime.of(workDate, time));
        patch.setReason(request.getReason().trim());
        patch.setStatus("PENDING");
        patch.setFlowId(flow.getId());
        return AttendancePatchDto.from(patchRepository.save(patch));
    }

    @Transactional(readOnly = true)
    public List<AttendancePatchDto> listPatches() {
        User user = requireUser();
        return patchRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(AttendancePatchDto::from).collect(Collectors.toList());
    }

    /** 审批流程结束后回写补卡状态，通过时自动修正考勤记录 */
    @EventListener
    @Transactional
    public void onApprovalFinished(ApprovalFinishedEvent event) {
        AttendancePatch patch = patchRepository.findByFlowId(event.getFlowId()).orElse(null);
        if (patch == null || !"PENDING".equals(patch.getStatus())) {
            return;
        }
        String comment = event.getComment();
        if ("APPROVED".equals(event.getStatus())) {
            try {
                applyPatch(patch);
                patch.setStatus("APPROVED");
                patch.setApproveComment(comment);
            } catch (IllegalArgumentException ex) {
                patch.setStatus("REJECTED");
                patch.setApproveComment("补卡失败：" + ex.getMessage());
            }
        } else {
            patch.setStatus("REJECTED");
            patch.setApproveComment("CANCELLED".equals(event.getStatus()) ? "申请人已撤回" : comment);
        }
        patch.setApproverName(event.getApproverName());
        patch.setApprovedAt(LocalDateTime.now());
        patchRepository.save(patch);
    }

    private void applyPatch(AttendancePatch patch) {
        AttendanceRecord record = recordRepository
                .findByUserIdAndWorkDate(patch.getUserId(), patch.getWorkDate())
                .orElse(null);
        if (record == null) {
            User user = userRepository.findById(patch.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("申请人不存在"));
            record = newRecord(user, patch.getWorkDate());
        }
        if ("IN".equals(patch.getPatchType())) {
            if (record.getCheckInTime() != null) {
                throw new IllegalArgumentException("该员工当日已有上班打卡记录，无需补卡");
            }
            record.setCheckInTime(patch.getPatchTime());
            record.setCheckInStatus("NORMAL");
        } else {
            if (record.getCheckOutTime() != null) {
                throw new IllegalArgumentException("该员工当日已有下班打卡记录，无需补卡");
            }
            record.setCheckOutTime(patch.getPatchTime());
            record.setCheckOutStatus("NORMAL");
        }
        if (record.getCheckInTime() != null && record.getCheckOutTime() != null
                && record.getCheckOutTime().isAfter(record.getCheckInTime())) {
            record.setWorkMinutes((int) Duration.between(
                    record.getCheckInTime(), record.getCheckOutTime()).toMinutes());
        }
        record.setRemark("含补卡");
        recordRepository.save(record);
    }

    // ==================== 打卡点配置 ====================

    @Transactional(readOnly = true)
    public AttendanceOfficeConfigDto getOfficeConfig() {
        requireAdmin();
        return AttendanceOfficeConfigDto.from(currentOfficeConfig());
    }

    @Transactional
    public AttendanceOfficeConfigDto saveOfficeConfig(AttendanceOfficeConfigRequest request) {
        requireAdmin();
        String name = request.getOfficeName() == null ? "" : request.getOfficeName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("请填写打卡点名称");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("打卡点名称不能超过50字");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("请填写打卡点经纬度");
        }
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("经纬度不合法");
        }
        Integer radius = request.getRadiusMeters();
        if (radius == null || radius < 10 || radius > 10000) {
            throw new IllegalArgumentException("打卡半径应在 10-10000 米之间");
        }
        AttendanceOfficeConfig config = officeConfigRepository.findAll().stream()
                .findFirst().orElseGet(AttendanceOfficeConfig::new);
        config.setOfficeName(name);
        config.setLatitude(lat);
        config.setLongitude(lng);
        config.setRadiusMeters(radius);
        config.setUpdatedBy(SecurityUtil.currentUsername());
        return AttendanceOfficeConfigDto.from(officeConfigRepository.save(config));
    }

    /** 当前生效的打卡点配置，数据库无记录时回退到 application.yml 默认值 */
    private AttendanceOfficeConfig currentOfficeConfig() {
        return officeConfigRepository.findAll().stream().findFirst().orElseGet(() -> {
            AttendanceOfficeConfig config = new AttendanceOfficeConfig();
            config.setOfficeName(officeName);
            config.setLatitude(officeLatitude);
            config.setLongitude(officeLongitude);
            config.setRadiusMeters(officeRadius);
            return config;
        });
    }

    // ==================== 辅助 ====================

    private AttendanceRecord newRecord(User user, LocalDate date) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(user.getId());
        record.setUsername(user.getUsername());
        record.setUserName(user.getNickname());
        record.setWorkDate(date);
        return record;
    }

    private User requireUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private void requireAdmin() {
        User user = requireUser();
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("只有管理员可以执行该操作");
        }
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("月份格式不正确，应为 yyyy-MM");
        }
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确，应为 yyyy-MM-dd");
        }
    }
}
