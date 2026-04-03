package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Booking;
import com.example.anzhuoapp.mapper.BookingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingMapper bookingMapper;

    @PostMapping("/submit")
    public String submitBooking(@RequestBody Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            Integer deviceId = (Integer) params.get("deviceId");
            Integer duration = (Integer) params.get("duration");
            String startTimeStr = (String) params.get("startTime"); // 格式：2026-04-04 12:00:00

            // 1. 权限与限制校验
            String deviceType = bookingMapper.getDeviceTypeById(deviceId);
            if (bookingMapper.checkTypeLimit(username, deviceType) > 0) {
                return "limit_error";
            }

            // 2. 计算结束时间
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTimeStr, df);
            LocalDateTime end = start.plusMinutes(duration);
            String endTimeStr = df.format(end);

            // 3. 写入数据库
            bookingMapper.createBooking(username, deviceId, startTimeStr, endTimeStr, duration);
            bookingMapper.updateDeviceStatus(deviceId);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/myList")
    public List<Booking> getMyList(@RequestParam String username) {
        return bookingMapper.getMyBookings(username);
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Integer bookingId = (Integer) params.get("bookingId");
            String action = (String) params.get("action");

            if ("cancel".equals(action)) {
                bookingMapper.updateBookingStatus(bookingId, "已取消");
                bookingMapper.updateDeviceStatusByBookingId(bookingId, "空闲");
            } else if ("start".equals(action)) {
                bookingMapper.updateBookingStatus(bookingId, "使用中");
            } else if ("finish".equals(action)) {
                bookingMapper.updateBookingStatus(bookingId, "已完成");
                bookingMapper.updateDeviceStatusByBookingId(bookingId, "待审核");
            }
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}