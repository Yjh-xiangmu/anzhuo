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
    private Integer reviewed; // 0=未评价 1=已评价

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public Integer getDeviceId() { return deviceId; }
    public Integer getDuration() { return duration; }
    public String getStatus() { return status; }
    public String getDeviceName() { return deviceName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Integer getReviewed() { return reviewed; }
}