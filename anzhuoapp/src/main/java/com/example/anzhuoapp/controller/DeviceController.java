package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Device;
import com.example.anzhuoapp.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceMapper deviceMapper;

    @GetMapping("/list")
    public List<Device> getDeviceList() { return deviceMapper.findAll(); }

    @GetMapping("/availableCount")
    public int getAvailableCount() { return deviceMapper.countAvailable(); }

    @GetMapping("/count")
    public int getTotalCount() { return deviceMapper.countAll(); }

    @PostMapping("/add")
    public String addDevice(@RequestBody Map<String, String> params) {
        try {
            deviceMapper.insert(params.get("deviceName"), params.get("deviceType"), params.get("location"));
            return "success";
        } catch (Exception e) { return "error"; }
    }

    @PostMapping("/update")
    public String updateDevice(@RequestBody Map<String, Object> params) {
        try {
            deviceMapper.update(
                    (Integer) params.get("id"),
                    (String) params.get("deviceName"),
                    (String) params.get("deviceType"),
                    (String) params.get("location"),
                    (String) params.get("status"));
            return "success";
        } catch (Exception e) { return "error"; }
    }

    // 专门用于锁定/解锁设备状态（只更新 status 字段）
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody Map<String, Object> params) {
        try {
            deviceMapper.updateStatusOnly(
                    (Integer) params.get("id"),
                    (String) params.get("status"));
            return "success";
        } catch (Exception e) { return "error"; }
    }

    @PostMapping("/delete")
    public String deleteDevice(@RequestBody Map<String, Integer> params) {
        try {
            deviceMapper.deleteById(params.get("id"));
            return "success";
        } catch (Exception e) { return "error"; }
    }
}