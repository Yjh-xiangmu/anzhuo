package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Booking;
import com.example.anzhuoapp.mapper.BookingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
            String startTimeStr = (String) params.get("startTime");

            String deviceType = bookingMapper.getDeviceTypeById(deviceId);
            if (bookingMapper.checkTypeLimit(username, deviceType) > 0) return "limit_error";

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTimeStr, df);
            LocalDateTime end = start.plusMinutes(duration);
            String endTimeStr = df.format(end);

            if (bookingMapper.checkTimeConflict(deviceId, startTimeStr, endTimeStr) > 0)
                return "conflict";

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

    @GetMapping("/allList")
    public List<Booking> getAllList() {
        return bookingMapper.getAllBookings();
    }

    @GetMapping("/activeCount")
    public int getActiveCount(@RequestParam String username) {
        return bookingMapper.getActiveCount(username);
    }

    @GetMapping("/pendingCount")
    public int getPendingCount() {
        return bookingMapper.getPendingAuditCount();
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats(@RequestParam String username) {
        Map<String, Integer> result = new HashMap<>();
        result.put("total", bookingMapper.getTotalCount(username));
        result.put("ongoing", bookingMapper.getActiveCount(username));
        result.put("done", bookingMapper.getDoneCount(username));
        return result;
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Integer bookingId = (Integer) params.get("bookingId");
            String action = (String) params.get("action");
            switch (action) {
                case "cancel":
                    bookingMapper.updateBookingStatus(bookingId, "已取消");
                    bookingMapper.updateDeviceStatusByBookingId(bookingId, "空闲");
                    break;
                case "start":
                    bookingMapper.updateBookingStatus(bookingId, "使用中");
                    break;
                case "finish":
                    bookingMapper.updateBookingStatus(bookingId, "已完成");
                    bookingMapper.updateDeviceStatusByBookingId(bookingId, "待审核");
                    break;
            }
            return "success";
        } catch (Exception e) { return "error"; }
    }

    // 管理员审核通过：设备恢复空闲
    @PostMapping("/approve")
    public String approve(@RequestBody Map<String, Integer> params) {
        try {
            Integer bookingId = params.get("bookingId");
            bookingMapper.updateBookingStatus(bookingId, "已审核");
            bookingMapper.updateDeviceStatusByBookingId(bookingId, "空闲");
            return "success";
        } catch (Exception e) { return "error"; }
    }
}