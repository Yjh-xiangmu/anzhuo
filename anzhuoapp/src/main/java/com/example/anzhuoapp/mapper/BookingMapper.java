package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Booking;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookingMapper {

    @Select("SELECT device_type FROM device WHERE id = #{deviceId}")
    String getDeviceTypeById(Integer deviceId);

    // 检查同一用户在同类型设备上是否已有未完成的预约
    @Select("SELECT COUNT(*) FROM booking b JOIN device d ON b.device_id = d.id " +
            "WHERE b.username = #{username} AND d.device_type = #{deviceType} " +
            "AND b.status IN ('未开始', '使用中')")
    int checkTypeLimit(String username, String deviceType);

    // 插入预约
    @Insert("INSERT INTO booking(username, device_id, start_time, end_time, duration, status) " +
            "VALUES(#{username}, #{deviceId}, #{startTime}, #{endTime}, #{duration}, '未开始')")
    void createBooking(String username, Integer deviceId, String startTime, String endTime, Integer duration);

    @Update("UPDATE device SET status = '已被预约' WHERE id = #{deviceId}")
    void updateDeviceStatus(Integer deviceId);

    // 核心修复点：明确将 start_time 映射为 startTime，并格式化时间
    @Select("SELECT b.id, b.username, b.device_id as deviceId, b.duration, b.status, " +
            "DATE_FORMAT(b.start_time, '%Y-%m-%d %H:%i') as startTime, " +
            "DATE_FORMAT(b.end_time, '%Y-%m-%d %H:%i') as endTime, " +
            "d.device_name as deviceName " +
            "FROM booking b JOIN device d ON b.device_id = d.id " +
            "WHERE b.username = #{username} ORDER BY b.id DESC")
    List<Booking> getMyBookings(String username);

    @Update("UPDATE booking SET status = #{status} WHERE id = #{bookingId}")
    void updateBookingStatus(Integer bookingId, String status);

    @Update("UPDATE device SET status = #{status} WHERE id = (SELECT device_id FROM booking WHERE id = #{bookingId})")
    void updateDeviceStatusByBookingId(Integer bookingId, String status);
}