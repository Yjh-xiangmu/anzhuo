package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Review;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReviewMapper {

    @Select("SELECT COUNT(*) FROM review WHERE booking_id = #{bookingId}")
    int hasReviewed(Integer bookingId);

    @Insert("INSERT INTO review(username, device_id, booking_id, rating, content) " +
            "VALUES(#{username}, #{deviceId}, #{bookingId}, #{rating}, #{content})")
    void insertReview(String username, Integer deviceId, Integer bookingId,
                      Integer rating, String content);

    @Update("UPDATE booking SET reviewed = 1 WHERE id = #{bookingId}")
    void markReviewed(Integer bookingId);

    @Select("SELECT id, username, device_id as deviceId, booking_id as bookingId, " +
            "rating, content, DATE_FORMAT(created_at,'%Y-%m-%d') as createdAt " +
            "FROM review WHERE device_id = #{deviceId} ORDER BY created_at DESC")
    List<Review> getDeviceReviews(Integer deviceId);

    @Select("SELECT r.id, r.username, r.device_id as deviceId, r.booking_id as bookingId, " +
            "r.rating, r.content, DATE_FORMAT(r.created_at,'%Y-%m-%d') as createdAt, " +
            "d.device_name as deviceName " +
            "FROM review r JOIN device d ON r.device_id = d.id " +
            "WHERE r.username = #{username} ORDER BY r.created_at DESC")
    List<Review> getMyReviews(String username);

    @Select("SELECT r.id, r.username, r.device_id as deviceId, r.booking_id as bookingId, " +
            "r.rating, r.content, DATE_FORMAT(r.created_at,'%Y-%m-%d') as createdAt, " +
            "d.device_name as deviceName " +
            "FROM review r JOIN device d ON r.device_id = d.id ORDER BY r.created_at DESC")
    List<Review> getAllReviews();

    @Delete("DELETE FROM review WHERE id = #{id}")
    void deleteReview(Integer id);
}