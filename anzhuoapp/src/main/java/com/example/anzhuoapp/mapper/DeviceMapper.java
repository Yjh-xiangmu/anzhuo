package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Device;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DeviceMapper {

    @Select("SELECT id, device_name as deviceName, device_type as deviceType, location, status FROM device")
    List<Device> findAll();

    @Select("SELECT COUNT(*) FROM device WHERE status = '空闲'")
    int countAvailable();

    @Select("SELECT COUNT(*) FROM device")
    int countAll();

    @Insert("INSERT INTO device(device_name, device_type, location, status) " +
            "VALUES(#{name}, #{type}, #{location}, '空闲')")
    void insert(String name, String type, String location);

    @Update("UPDATE device SET device_name=#{name}, device_type=#{type}, " +
            "location=#{location}, status=#{status} WHERE id=#{id}")
    void update(Integer id, String name, String type, String location, String status);

    // 只更新状态（锁定/解锁用）
    @Update("UPDATE device SET status=#{status} WHERE id=#{id}")
    void updateStatusOnly(Integer id, String status);

    @Delete("DELETE FROM device WHERE id = #{id}")
    void deleteById(Integer id);
}