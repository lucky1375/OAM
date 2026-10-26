package com.oam.attendance.dto;

public class AttendanceLocationRequest {

    private Double latitude;
    private Double longitude;
    /** 定位地址描述（可选） */
    private String address;

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
