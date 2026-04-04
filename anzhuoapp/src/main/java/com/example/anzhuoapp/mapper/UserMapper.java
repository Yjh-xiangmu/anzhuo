package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User login(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO user(username, password, role) VALUES(#{username}, #{password}, 'student')")
    int register(@Param("username") String username, @Param("password") String password);

    @Update("UPDATE user SET password = #{newPassword} WHERE username = #{username}")
    void updatePassword(@Param("username") String username, @Param("newPassword") String newPassword);

    @Select("SELECT id, username, role FROM user ORDER BY id")
    List<User> findAll();
}