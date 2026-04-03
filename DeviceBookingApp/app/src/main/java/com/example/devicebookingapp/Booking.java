package com.example.devicebookingapp;

public class Booking {
    private Integer id;
    private String username;
    private Integer deviceId;
    private Integer duration;
    private String status;
    private String deviceName;
    private String startTime;
    private String endTime;

    public Integer getId() { return id; }
    public String getStatus() { return status; }
    public String getDeviceName() { return deviceName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Integer getDuration() { return duration; }
}