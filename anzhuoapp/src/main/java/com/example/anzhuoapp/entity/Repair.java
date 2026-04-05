package com.example.anzhuoapp.entity;

public class Repair {
    private Integer id;
    private String username;
    private Integer deviceId;
    private String deviceName;
    private String description;

    // 把缺失的图片地址字段补上
    private String imageUrl;

    private String status;
    private String createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // 加上对应的 get 和 set 方法
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}