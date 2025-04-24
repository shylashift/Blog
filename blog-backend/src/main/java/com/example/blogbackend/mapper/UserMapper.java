package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息（可选）
     */
    @Select("SELECT u.*, " +
           "(SELECT TOP 1 CASE r.RoleId " +
           "    WHEN 1 THEN '管理员' " +
           "    WHEN 2 THEN '普通用户' " +
           "    ELSE r.Description " +
           "END " +
           "FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = u.UserId) as roles " +
           "FROM Users u " +
           "WHERE u.Email = #{email}")
    @Results({
        @Result(property = "userId", column = "UserId"),
        @Result(property = "username", column = "Username"),
        @Result(property = "password", column = "Password"),
        @Result(property = "email", column = "Email"),
        @Result(property = "avatar", column = "Avatar"),
        @Result(property = "bio", column = "Bio"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "disabled", column = "Disabled"),
        @Result(property = "roles", column = "roles")
    })
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT u.*, " +
           "(SELECT TOP 1 CASE r.RoleId " +
           "    WHEN 1 THEN '管理员' " +
           "    WHEN 2 THEN '普通用户' " +
           "    ELSE r.Description " +
           "END " +
           "FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = u.UserId) as roles " +
           "FROM Users u " +
           "WHERE u.Username = #{username}")
    @Results({
        @Result(property = "userId", column = "UserId"),
        @Result(property = "username", column = "Username"),
        @Result(property = "password", column = "Password"),
        @Result(property = "email", column = "Email"),
        @Result(property = "avatar", column = "Avatar"),
        @Result(property = "bio", column = "Bio"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "disabled", column = "Disabled"),
        @Result(property = "roles", column = "roles")
    })
    Optional<User> findByUsername(String username);

    /**
     * 根据ID查找用户（包含角色信息）
     * @param userId 用户ID
     * @return 用户信息（可选）
     */
    @Select("SELECT u.*, " +
           "(SELECT STRING_AGG(r.RoleName, ',') " +
           "FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = u.UserId) as roles " +
           "FROM Users u " +
           "WHERE u.UserId = #{userId}")
    @Results({
        @Result(property = "userId", column = "UserId"),
        @Result(property = "username", column = "Username"),
        @Result(property = "password", column = "Password"),
        @Result(property = "email", column = "Email"),
        @Result(property = "avatar", column = "Avatar"),
        @Result(property = "bio", column = "Bio"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "disabled", column = "Disabled"),
        @Result(property = "roles", column = "roles")
    })
    Optional<User> findByIdWithRoles(Integer userId);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM Users WHERE Email = #{email}")
    int countByEmail(String email);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM Users WHERE Username = #{username}")
    int countByUsername(String username);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新的行数
     */
    @Update("UPDATE Users SET " +
            "Username = #{username}, " +
            "Email = #{email}, " +
            "Avatar = #{avatar}, " +
            "Bio = #{bio}, " +
            "UpdatedAt = #{updatedAt}, " +
            "Disabled = #{disabled} " +
            "WHERE UserId = #{userId}")
    int updateUserById(User user);

    /**
     * 根据ID查找用户，返回基本信息（不包含角色）
     * @param userId 用户ID
     * @return 用户信息
     */
    @Select("SELECT UserId, Username, Email, Avatar, Bio, CreatedAt " +
           "FROM Users " +
           "WHERE UserId = #{userId}")
    User findUserById(Integer userId);
    
    /**
     * 查询所有用户（包含角色信息），带分页
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    @Select("SELECT u.*, " +
           "(SELECT TOP 1 CASE r.RoleId " +
           "    WHEN 1 THEN '管理员' " +
           "    WHEN 2 THEN '普通用户' " +
           "    ELSE r.Description " +
           "END " +
           "FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = u.UserId) as roles " +
           "FROM Users u " +
           "ORDER BY u.CreatedAt DESC " +
           "OFFSET #{offset} ROWS " +
           "FETCH NEXT #{limit} ROWS ONLY")
    @Results({
        @Result(property = "userId", column = "UserId"),
        @Result(property = "username", column = "Username"),
        @Result(property = "email", column = "Email"),
        @Result(property = "avatar", column = "Avatar"),
        @Result(property = "bio", column = "Bio"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "disabled", column = "Disabled"),
        @Result(property = "roles", column = "roles")
    })
    List<User> findAllWithRoles(int offset, int limit);
    
    /**
     * 根据关键词搜索用户（包含角色信息），带分页
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    @Select("SELECT u.*, " +
           "(SELECT TOP 1 CASE r.RoleId " +
           "    WHEN 1 THEN '管理员' " +
           "    WHEN 2 THEN '普通用户' " +
           "    ELSE r.Description " +
           "END " +
           "FROM UserRoles ur " +
           "JOIN Roles r ON ur.RoleId = r.RoleId " +
           "WHERE ur.UserId = u.UserId) as roles " +
           "FROM Users u " +
           "WHERE u.Username LIKE CONCAT('%', #{keyword}, '%') " +
           "OR u.Email LIKE CONCAT('%', #{keyword}, '%') " +
           "ORDER BY u.CreatedAt DESC " +
           "OFFSET #{offset} ROWS " +
           "FETCH NEXT #{limit} ROWS ONLY")
    @Results({
        @Result(property = "userId", column = "UserId"),
        @Result(property = "username", column = "Username"),
        @Result(property = "email", column = "Email"),
        @Result(property = "avatar", column = "Avatar"),
        @Result(property = "bio", column = "Bio"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "disabled", column = "Disabled"),
        @Result(property = "roles", column = "roles")
    })
    List<User> findByKeywordWithRoles(String keyword, int offset, int limit);
    
    /**
     * 根据关键词统计用户数量
     * @param keyword 关键词
     * @return 用户数量
     */
    @Select("SELECT COUNT(DISTINCT u.UserId) FROM Users u " +
          "WHERE u.Username LIKE CONCAT('%', #{keyword}, '%') " +
          "OR u.Email LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(String keyword);
} 