package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface FavoriteRepository extends BaseMapper<Favorite> {
    @Select("SELECT * FROM Favorites WHERE UserId = #{userId} AND PostId = #{postId}")
    Optional<Favorite> findByUserIdAndPostId(Integer userId, Integer postId);
    
    @Select("SELECT COUNT(*) FROM Favorites WHERE PostId = #{postId}")
    Integer countByPostId(Integer postId);
} 