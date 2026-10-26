package com.oam.attendance.dto;

import com.oam.attendance.entity.AttendanceOfficeConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 打卡点配置 */
public class AttendanceOfficeConfigDto {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String officeName;
    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;
    private String updatedBy;
    private String updatedAt;

    public static AttendanceOfficeConfigDto from(AttendanceOfficeConfig config) {
        AttendanceOfficeConfigDto dto = new AttendanceOfficeConfigDto();
        dto.setOfficeName(config.getOfficeName());
        dto.setLatitude(config.getLatitude());
        dto.setLongitude(config.getLongitude());
        dto.setRadiusMeters(config.getRadiusMeters());
        dto.setUpdatedBy(config.getUpdatedBy());
        LocalDateTime updatedAt = config.getUpdatedAt();
        dto.setUpdatedAt(updatedAt == null ? null : updatedAt.format(TIME_FMT));
        return dto;
    }

    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
