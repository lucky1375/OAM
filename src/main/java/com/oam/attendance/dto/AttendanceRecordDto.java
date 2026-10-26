package com.oam.attendance.dto;

import com.oam.attendance.entity.AttendanceRecord;

import java.time.format.DateTimeFormatter;

public class AttendanceRecordDto {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;
    private String workDate;
    private Long userId;
    private String username;
    private String userName;
    private String checkInTime;
    private String checkOutTime;
    private String checkInStatus;
    private String checkOutStatus;
    private Integer workMinutes;
    private String workDuration;
    private String remark;
    private String checkInAddress;
    private String checkOutAddress;
    private boolean checked;

    public static AttendanceRecordDto from(AttendanceRecord r) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.id = r.getId();
        dto.workDate = r.getWorkDate() == null ? null : r.getWorkDate().format(DATE_FMT);
        dto.userId = r.getUserId();
        dto.username = r.getUsername();
        dto.userName = r.getUserName();
        dto.checkInTime = r.getCheckInTime() == null ? null : r.getCheckInTime().format(TIME_FMT);
        dto.checkOutTime = r.getCheckOutTime() == null ? null : r.getCheckOutTime().format(TIME_FMT);
        dto.checkInStatus = r.getCheckInStatus();
        dto.checkOutStatus = r.getCheckOutStatus();
        dto.workMinutes = r.getWorkMinutes();
        dto.workDuration = formatDuration(r.getWorkMinutes());
        dto.remark = r.getRemark();
        dto.checkInAddress = r.getCheckInAddress();
        dto.checkOutAddress = r.getCheckOutAddress();
        dto.checked = true;
        return dto;
    }

    public static String formatDuration(Integer minutes) {
        if (minutes == null) return null;
        return minutes / 60 + "小时" + (minutes % 60 == 0 ? "" : (minutes % 60) + "分");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getCheckInStatus() { return checkInStatus; }
    public void setCheckInStatus(String checkInStatus) { this.checkInStatus = checkInStatus; }
    public String getCheckOutStatus() { return checkOutStatus; }
    public void setCheckOutStatus(String checkOutStatus) { this.checkOutStatus = checkOutStatus; }
    public Integer getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { this.workMinutes = workMinutes; }
    public String getWorkDuration() { return workDuration; }
    public void setWorkDuration(String workDuration) { this.workDuration = workDuration; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCheckInAddress() { return checkInAddress; }
    public void setCheckInAddress(String checkInAddress) { this.checkInAddress = checkInAddress; }
    public String getCheckOutAddress() { return checkOutAddress; }
    public void setCheckOutAddress(String checkOutAddress) { this.checkOutAddress = checkOutAddress; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
