package com.oam.attendance.dto;

/** 月度考勤统计 */
public class AttendanceStatsDto {

    private String month;
    private int workDays;
    private int lateCount;
    private int earlyCount;
    private int totalMinutes;
    private String avgDuration;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public int getWorkDays() { return workDays; }
    public void setWorkDays(int workDays) { this.workDays = workDays; }
    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }
    public int getEarlyCount() { return earlyCount; }
    public void setEarlyCount(int earlyCount) { this.earlyCount = earlyCount; }
    public int getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }
    public String getAvgDuration() { return avgDuration; }
    public void setAvgDuration(String avgDuration) { this.avgDuration = avgDuration; }
}
