package com.oam.attendance.dto;

/** 打卡点配置保存请求 */
public class AttendanceOfficeConfigRequest {

    private String officeName;
    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;

    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }
}
