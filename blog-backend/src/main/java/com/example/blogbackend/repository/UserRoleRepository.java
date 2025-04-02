package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Role;
import com.example.blogbackend.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRoleRepository extends BaseMapper<UserRole> {
    @Select("SELECT r.* FROM Roles r " +
           "INNER JOIN UserRoles ur ON r.RoleId = ur.RoleId " +
           "WHERE ur.UserId = #{userId}")
    List<Role> findRolesByUserId(Integer userId);

    @Select("SELECT * FROM UserRoles WHERE UserId = #{userId} AND RoleId = #{roleId}")
    Optional<UserRole> findByUserIdAndRoleId(Integer userId, Integer roleId);
} 