package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Role;
import com.example.blogbackend.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID查找用户角色关系
     * @param userId 用户ID
     * @return 用户角色关系列表
     */
    @Select("SELECT * FROM UserRoles WHERE UserId = #{userId}")
    List<UserRole> findByUserId(Integer userId);

    /**
     * 根据角色ID查找用户角色关系
     * @param roleId 角色ID
     * @return 用户角色关系列表
     */
    @Select("SELECT * FROM UserRoles WHERE RoleId = #{roleId}")
    List<UserRole> findByRoleId(Integer roleId);

    @Select("SELECT r.* FROM Roles r " +
           "JOIN UserRoles ur ON r.RoleId = ur.RoleId " +
           "WHERE ur.UserId = #{userId}")
    List<Role> findRolesByUserId(Integer userId);
} 