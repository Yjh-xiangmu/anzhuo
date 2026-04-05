package com.example.devicebookingapp;

import com.google.gson.annotations.SerializedName;

public class Repair {
    private Integer id;
    private String username;
    private Integer deviceId;
    private String deviceName;
    private String description;

    // 加上这个注解，兼容后端传过来的 image_url
    @SerializedName(value = "imageUrl", alternate = {"image_url"})
    private String imageUrl;

    private String status;
    private String createdAt;

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public Integer getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}