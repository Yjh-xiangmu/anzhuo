package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DeviceMapper {

    // 查询所有设备列表
    @Select("SELECT id, device_name as deviceName, device_type as deviceType, location, status FROM device")
    List<Device> findAll();
}