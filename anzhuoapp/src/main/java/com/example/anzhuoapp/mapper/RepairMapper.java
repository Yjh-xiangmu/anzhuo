package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Repair;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RepairMapper {

    @Insert("INSERT INTO repair(username, device_id, description, image_url, status) " +
            "VALUES(#{username}, #{deviceId}, #{description}, #{imageUrl}, '待处理')")
    void submitRepair(String username, Integer deviceId, String description, String imageUrl);

    @Select("SELECT r.id, r.username, r.device_id as deviceId, r.description, " +
            "r.image_url as imageUrl, r.status, " +
            "DATE_FORMAT(r.created_at,'%Y-%m-%d') as createdAt, d.device_name as deviceName " +
            "FROM repair r JOIN device d ON r.device_id = d.id " +
            "WHERE r.username = #{username} ORDER BY r.created_at DESC")
    List<Repair> getMyRepairs(String username);

    @Select("SELECT r.id, r.username, r.device_id as deviceId, r.description, " +
            "r.image_url as imageUrl, r.status, " +
            "DATE_FORMAT(r.created_at,'%Y-%m-%d') as createdAt, d.device_name as deviceName " +
            "FROM repair r JOIN device d ON r.device_id = d.id ORDER BY r.created_at DESC")
    List<Repair> getAllRepairs();

    @Select("SELECT COUNT(*) FROM repair WHERE status = '待处理'")
    int getPendingCount();

    @Select("SELECT COUNT(*) FROM repair WHERE username = #{username}")
    int countByUser(String username);

    @Update("UPDATE repair SET status = #{status} WHERE id = #{id}")
    void updateRepairStatus(Integer id, String status);
}