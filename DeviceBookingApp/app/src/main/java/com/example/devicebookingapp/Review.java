package com.example.devicebookingapp;

public class Review {
    private Integer id;
    private String username;
    private Integer deviceId;
    private Integer bookingId;
    private Integer rating;
    private String content;
    private String createdAt;
    private String deviceName;  // ← 这行

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public Integer getDeviceId() { return deviceId; }
    public Integer getBookingId() { return bookingId; }
    public Integer getRating() { return rating; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
    public String getDeviceName() { return deviceName; }  // ← 这行
}