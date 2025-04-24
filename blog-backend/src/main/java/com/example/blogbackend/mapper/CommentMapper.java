package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 获取文章的所有评论
     * @param postId 文章ID
     * @return 评论列表
     */
    @Select("SELECT c.CommentId, c.PostId, c.UserId, c.Content, c.CreatedAt, c.UpdatedAt, " +
           "u.Username as author_name " +
           "FROM Comments c " +
           "LEFT JOIN Users u ON c.UserId = u.UserId " +
           "WHERE c.PostId = #{postId} " +
           "ORDER BY c.CreatedAt DESC")
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "user.username", column = "author_name")
    })
    List<Comment> getCommentsByPostId(Integer postId);

    /**
     * 获取用户的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT c.CommentId, c.PostId, c.UserId, c.Content, c.CreatedAt, c.UpdatedAt, " +
           "p.Title as post_title " +
           "FROM Comments c " +
           "LEFT JOIN Posts p ON c.PostId = p.PostId " +
           "WHERE c.UserId = #{userId} " +
           "ORDER BY c.CreatedAt DESC")
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "post.title", column = "post_title")
    })
    List<Comment> getCommentsByUserId(Integer userId);

    /**
     * 查找文章的所有评论及其作者信息
     * @param postId 文章ID
     * @return 评论列表
     */
    @Select("SELECT c.CommentId, c.PostId, c.UserId, c.Content, c.CreatedAt, c.UpdatedAt, " +
           "u.Username as user_username, " +
           "u.Avatar as user_avatar, " +
           "u.UserId as user_userId " +
           "FROM Comments c " +
           "LEFT JOIN Users u ON c.UserId = u.UserId " +
           "WHERE c.PostId = #{postId} " +
           "ORDER BY c.CreatedAt DESC")
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "user", column = "UserId", javaType = User.class, 
            one = @One(select = "com.example.blogbackend.mapper.UserMapper.findUserById"))
    })
    List<Comment> findByPostIdWithUser(Integer postId);

    /**
     * 查找用户的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT c.CommentId, c.PostId, c.UserId, c.Content, c.CreatedAt, c.UpdatedAt, " +
           "p.Title as post_title, p.UserId as post_userId " +
           "FROM Comments c " +
           "LEFT JOIN Posts p ON c.PostId = p.PostId " +
           "WHERE c.UserId = #{userId} " +
           "ORDER BY c.CreatedAt DESC")
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "user", column = "UserId", javaType = User.class, 
            one = @One(select = "com.example.blogbackend.mapper.UserMapper.findUserById")),
        @Result(property = "post.title", column = "post_title"),
        @Result(property = "post.userId", column = "post_userId")
    })
    List<Comment> findByUserIdWithUserAndPost(Integer userId);

    /**
     * 查找评论及其作者信息
     * @param commentId 评论ID
     * @return 评论信息
     */
    @Select("SELECT c.CommentId, c.PostId, c.UserId, c.Content, c.CreatedAt, c.UpdatedAt " + 
           "FROM Comments c " +
           "WHERE c.CommentId = #{commentId}")
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "user", column = "UserId", javaType = User.class, 
            one = @One(select = "com.example.blogbackend.mapper.UserMapper.findUserById"))
    })
    Comment findByIdWithUser(Integer commentId);

    @Select({
        "<script>",
        "SELECT c.*, u.Username as author_name, u.Avatar as author_avatar, p.Title as post_title",
        "FROM Comments c",
        "LEFT JOIN Users u ON c.UserId = u.UserId",
        "LEFT JOIN Posts p ON c.PostId = p.PostId",
        "WHERE 1=1",
        "<if test='keyword != null and keyword != \"\"'>",
        "AND (c.Content LIKE CONCAT('%', #{keyword}, '%')",
        "OR u.Username LIKE CONCAT('%', #{keyword}, '%')",
        "OR p.Title LIKE CONCAT('%', #{keyword}, '%'))",
        "</if>",
        "ORDER BY c.CreatedAt DESC",
        "OFFSET #{offset} ROWS",
        "FETCH NEXT #{size} ROWS ONLY",
        "</script>"
    })
    @Results({
        @Result(property = "commentId", column = "CommentId"),
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "content", column = "Content"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "authorName", column = "author_name"),
        @Result(property = "authorAvatar", column = "author_avatar"),
        @Result(property = "postTitle", column = "post_title")
    })
    List<Comment> findAllWithDetails(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM Comments c",
        "LEFT JOIN Users u ON c.UserId = u.UserId",
        "LEFT JOIN Posts p ON c.PostId = p.PostId",
        "WHERE 1=1",
        "<if test='keyword != null and keyword != \"\"'>",
        "AND (c.Content LIKE CONCAT('%', #{keyword}, '%')",
        "OR u.Username LIKE CONCAT('%', #{keyword}, '%')",
        "OR p.Title LIKE CONCAT('%', #{keyword}, '%'))",
        "</if>",
        "</script>"
    })
    int getCommentCount(@Param("keyword") String keyword);
} 