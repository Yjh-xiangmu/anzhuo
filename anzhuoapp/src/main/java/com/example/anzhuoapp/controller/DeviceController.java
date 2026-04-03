package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Device;
import com.example.anzhuoapp.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceMapper deviceMapper;

    // 获取设备列表接口
    @GetMapping("/list")
    public List<Device> getDeviceList() {
        return deviceMapper.findAll();
    }
}