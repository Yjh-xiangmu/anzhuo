package com.example.anzhuoapp.scheduler;

import com.example.anzhuoapp.mapper.BookingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约自动过期定时任务
 * 每分钟检查一次：若预约开始时间已过且状态仍为"未开始"，自动取消并释放设备
 */
@Component
public class BookingScheduler {

    @Autowired
    private BookingMapper bookingMapper;

    // 每60秒执行一次
    @Scheduled(fixedRate = 60000)
    public void autoExpireBookings() {
        try {
            int count = bookingMapper.expireOverdueBookings();
            if (count > 0) {
                System.out.println("[定时任务] 自动取消过期预约 " + count + " 条，设备已释放");
            }
        } catch (Exception e) {
            System.err.println("[定时任务] 自动过期失败: " + e.getMessage());
        }
    }
}