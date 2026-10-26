package com.oam.attendance.dto;

/** 今日打卡状态 */
public class AttendanceTodayDto {

    private String date;
    private String serverTime;
    private String workStart;
    private String workEnd;
    private AttendanceRecordDto record;
    private boolean canCheckIn;
    private boolean canCheckOut;
    /** 打卡点配置，前端用于计算距离 */
    private String officeName;
    private Double officeLatitude;
    private Double officeLongitude;
    private Integer radiusMeters;
    /** 百度地图 AK，为空时前端降级为浏览器定位 */
    private String baiduAk;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getServerTime() { return serverTime; }
    public void setServerTime(String serverTime) { this.serverTime = serverTime; }
    public String getWorkStart() { return workStart; }
    public void setWorkStart(String workStart) { this.workStart = workStart; }
    public String getWorkEnd() { return workEnd; }
    public void setWorkEnd(String workEnd) { this.workEnd = workEnd; }
    public AttendanceRecordDto getRecord() { return record; }
    public void setRecord(AttendanceRecordDto record) { this.record = record; }
    public boolean isCanCheckIn() { return canCheckIn; }
    public void setCanCheckIn(boolean canCheckIn) { this.canCheckIn = canCheckIn; }
    public boolean isCanCheckOut() { return canCheckOut; }
    public void setCanCheckOut(boolean canCheckOut) { this.canCheckOut = canCheckOut; }
    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }
    public Double getOfficeLatitude() { return officeLatitude; }
    public void setOfficeLatitude(Double officeLatitude) { this.officeLatitude = officeLatitude; }
    public Double getOfficeLongitude() { return officeLongitude; }
    public void setOfficeLongitude(Double officeLongitude) { this.officeLongitude = officeLongitude; }
    public Integer getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }
    public String getBaiduAk() { return baiduAk; }
    public void setBaiduAk(String baiduAk) { this.baiduAk = baiduAk; }
}
