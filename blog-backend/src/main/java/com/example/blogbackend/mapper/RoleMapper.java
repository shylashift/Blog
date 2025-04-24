package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

import java.util.Optional;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色名查找角色
     * @param roleName 角色名
     * @return 角色信息
     */
    @Select("SELECT * FROM Roles WHERE RoleName = #{roleName}")
    Optional<Role> findByRoleName(String roleName);

    /**
     * 根据用户ID查找该用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM Roles r " +
           "INNER JOIN UserRoles ur ON r.RoleId = ur.RoleId " +
           "WHERE ur.UserId = #{userId}")
    List<Role> findRolesByUserId(Integer userId);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = #{userId} AND r.RoleName = 'ROLE_ADMIN'")
    boolean hasAdminRole(Integer userId);

    @Insert("INSERT INTO UserRoles (UserId, RoleId) " +
           "SELECT #{userId}, RoleId FROM Roles WHERE RoleName = 'ROLE_ADMIN'")
    void assignAdminRole(Integer userId);

    @Delete("DELETE FROM UserRoles WHERE UserId = #{userId} AND " +
           "RoleId = (SELECT RoleId FROM Roles WHERE RoleName = 'ROLE_ADMIN')")
    void removeAdminRole(Integer userId);
} 