package com.example.anzhuoapp.mapper;

import com.example.anzhuoapp.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User login(@Param("username") String username, @Param("password") String password);

    // 查询账号是否已经被注册
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    // 注册新用户，默认角色设为普通用户 student
    @Insert("INSERT INTO user(username, password, role) VALUES(#{username}, #{password}, 'student')")
    int register(@Param("username") String username, @Param("password") String password);
}