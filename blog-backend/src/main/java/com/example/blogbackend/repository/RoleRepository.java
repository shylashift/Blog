package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface RoleRepository extends BaseMapper<Role> {
    @Select("SELECT * FROM Roles WHERE RoleName = #{roleName}")
    Optional<Role> findByRoleName(String roleName);
} 