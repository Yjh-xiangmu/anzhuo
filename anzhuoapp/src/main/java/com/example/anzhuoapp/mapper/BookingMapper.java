package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Booking;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookingMapper {

    @Select("SELECT device_type FROM device WHERE id = #{deviceId}")
    String getDeviceTypeById(Integer deviceId);

    @Select("SELECT COUNT(*) FROM booking b JOIN device d ON b.device_id = d.id " +
            "WHERE b.username = #{username} AND d.device_type = #{deviceType} " +
            "AND b.status IN ('未开始', '使用中')")
    int checkTypeLimit(String username, String deviceType);

    @Select("SELECT COUNT(*) FROM booking WHERE device_id = #{deviceId} " +
            "AND status IN ('未开始', '使用中') " +
            "AND NOT (end_time <= #{startTime} OR start_time >= #{endTime})")
    int checkTimeConflict(Integer deviceId, String startTime, String endTime);

    @Insert("INSERT INTO booking(username, device_id, start_time, end_time, duration, status) " +
            "VALUES(#{username}, #{deviceId}, #{startTime}, #{endTime}, #{duration}, '未开始')")
    void createBooking(String username, Integer deviceId, String startTime,
                       String endTime, Integer duration);

    @Update("UPDATE device SET status = '已被预约' WHERE id = #{deviceId}")
    void updateDeviceStatus(Integer deviceId);

    @Select("SELECT b.id, b.username, b.device_id as deviceId, b.duration, b.status, " +
            "IFNULL(b.reviewed, 0) as reviewed, " +
            "DATE_FORMAT(b.start_time, '%Y-%m-%d %H:%i') as startTime, " +
            "DATE_FORMAT(b.end_time, '%Y-%m-%d %H:%i') as endTime, " +
            "d.device_name as deviceName " +
            "FROM booking b JOIN device d ON b.device_id = d.id " +
            "WHERE b.username = #{username} ORDER BY b.id DESC")
    List<Booking> getMyBookings(String username);

    @Select("SELECT b.id, b.username, b.device_id as deviceId, b.duration, b.status, " +
            "IFNULL(b.reviewed, 0) as reviewed, " +
            "DATE_FORMAT(b.start_time, '%Y-%m-%d %H:%i') as startTime, " +
            "DATE_FORMAT(b.end_time, '%Y-%m-%d %H:%i') as endTime, " +
            "d.device_name as deviceName " +
            "FROM booking b JOIN device d ON b.device_id = d.id ORDER BY b.id DESC")
    List<Booking> getAllBookings();

    @Select("SELECT COUNT(*) FROM booking WHERE status = '已完成'")
    int getPendingAuditCount();

    @Select("SELECT COUNT(*) FROM booking WHERE username=#{username} AND status IN ('未开始','使用中')")
    int getActiveCount(String username);

    @Select("SELECT COUNT(*) FROM booking WHERE username=#{username}")
    int getTotalCount(String username);

    @Select("SELECT COUNT(*) FROM booking WHERE username=#{username} AND status='已完成'")
    int getDoneCount(String username);

    @Update("UPDATE booking SET status = #{status} WHERE id = #{bookingId}")
    void updateBookingStatus(Integer bookingId, String status);

    @Update("UPDATE device SET status = #{status} WHERE id = " +
            "(SELECT device_id FROM booking WHERE id = #{bookingId})")
    void updateDeviceStatusByBookingId(Integer bookingId, String status);

    @Update("UPDATE booking SET reviewed = 1 WHERE id = #{bookingId}")
    void markReviewed(Integer bookingId);

    /**
     * 自动过期：开始时间已过 & 状态仍为"未开始" → 改为"已取消"，设备恢复"空闲"
     * 使用 UPDATE JOIN 一次搞定两张表
     */
    @Update("UPDATE booking b JOIN device d ON b.device_id = d.id " +
            "SET b.status = '已取消', d.status = '空闲' " +
            "WHERE b.status = '未开始' AND b.start_time < NOW()")
    int expireOverdueBookings();
}