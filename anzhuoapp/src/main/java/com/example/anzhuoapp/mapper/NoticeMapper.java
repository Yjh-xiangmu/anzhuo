package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.Notice;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NoticeMapper {

    @Select("SELECT id, title, content, DATE_FORMAT(created_at, '%Y-%m-%d') as createdAt " +
            "FROM notice ORDER BY created_at DESC LIMIT #{limit}")
    List<Notice> findAll(int limit);

    @Insert("INSERT INTO notice(title, content) VALUES(#{title}, #{content})")
    void insert(String title, String content);

    @Delete("DELETE FROM notice WHERE id = #{id}")
    void deleteById(Integer id);

    @Update("UPDATE notice SET title=#{title}, content=#{content} WHERE id=#{id}")
    void update(Integer id, String title, String content);
}