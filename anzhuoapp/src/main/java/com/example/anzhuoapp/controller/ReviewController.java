package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Review;
import com.example.anzhuoapp.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @PostMapping("/submit")
    public String submit(@RequestBody Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            Integer deviceId = (Integer) params.get("deviceId");
            Integer bookingId = (Integer) params.get("bookingId");
            Integer rating = (Integer) params.get("rating");
            String content = (String) params.get("content");
            if (reviewMapper.hasReviewed(bookingId) > 0) return "already_reviewed";
            reviewMapper.insertReview(username, deviceId, bookingId, rating, content);
            reviewMapper.markReviewed(bookingId);
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "error"; }
    }

    @GetMapping("/deviceList")
    public List<Review> getDeviceReviews(@RequestParam Integer deviceId) {
        return reviewMapper.getDeviceReviews(deviceId);
    }

    @GetMapping("/myList")
    public List<Review> getMyReviews(@RequestParam String username) {
        return reviewMapper.getMyReviews(username);
    }

    @GetMapping("/allList")
    public List<Review> getAllReviews() {
        return reviewMapper.getAllReviews();
    }

    @PostMapping("/delete")
    public String delete(@RequestBody Map<String, Integer> params) {
        try {
            reviewMapper.deleteReview(params.get("id"));
            return "success";
        } catch (Exception e) { return "error"; }
    }
}