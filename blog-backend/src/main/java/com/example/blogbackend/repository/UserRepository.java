package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    @Select("SELECT u.*, r.RoleName FROM Users u " +
            "LEFT JOIN UserRoles ur ON u.UserId = ur.UserId " +
            "LEFT JOIN Roles r ON ur.RoleId = r.RoleId " +
            "WHERE u.Email = #{email}")
    Optional<User> findByEmail(String email);
    
    @Select("SELECT u.*, r.RoleName FROM Users u " +
            "LEFT JOIN UserRoles ur ON u.UserId = ur.UserId " +
            "LEFT JOIN Roles r ON ur.RoleId = r.RoleId " +
            "WHERE u.Username = #{username}")
    Optional<User> findByUsername(String username);
} 