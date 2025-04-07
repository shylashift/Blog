package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    /**
     * 查找文章及其作者信息
     * @param postId 文章ID
     * @return 文章及作者信息
     */
    @Select("SELECT p.*, u.Username, u.Avatar, u.UserId as UserID, " +
           "(SELECT COUNT(*) FROM Comments WHERE PostId = p.PostId) as CommentCount " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.PostId = #{postId}")
    @Results({
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "title", column = "Title"),
        @Result(property = "content", column = "Content"),
        @Result(property = "summary", column = "Summary"),
        @Result(property = "tags", column = "Tags"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "authorName", column = "Username"),
        @Result(property = "authorAvatar", column = "Avatar"),
        @Result(property = "author.userId", column = "UserID"),
        @Result(property = "author.username", column = "Username"),
        @Result(property = "author.avatar", column = "Avatar"),
        @Result(property = "commentCount", column = "CommentCount")
    })
    Post findPostWithUser(Integer postId);

    /**
     * 查找所有文章及其作者信息
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username, u.Avatar, u.UserId as UserID, " +
           "(SELECT COUNT(*) FROM Comments WHERE PostId = p.PostId) as CommentCount " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "ORDER BY p.CreatedAt DESC")
    @Results({
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "title", column = "Title"),
        @Result(property = "content", column = "Content"),
        @Result(property = "summary", column = "Summary"),
        @Result(property = "tags", column = "Tags"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "authorName", column = "Username"),
        @Result(property = "authorAvatar", column = "Avatar"),
        @Result(property = "author.userId", column = "UserID"),
        @Result(property = "author.username", column = "Username"),
        @Result(property = "author.avatar", column = "Avatar"),
        @Result(property = "commentCount", column = "CommentCount")
    })
    List<Post> findAllWithUser();

    /**
     * 查找用户的所有文章
     * @param userId 用户ID
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username as \"author.username\", " +
           "u.Avatar as \"author.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.UserId = #{userId} " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> findByUserId(Integer userId);

    /**
     * 搜索文章
     * @param keyword 关键词
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username as \"author.username\", " +
           "u.Avatar as \"author.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Content LIKE CONCAT('%', #{keyword}, '%') " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> searchPosts(String keyword);

    /**
     * 获取所有标签
     * @return 标签列表
     */
    @Select("WITH TagData AS (" +
           "    SELECT Tags " +
           "    FROM Posts " +
           "    WHERE Tags IS NOT NULL AND Tags <> ''" +
           ") " +
           "SELECT DISTINCT TRIM(value) AS tag " +
           "FROM TagData " +
           "CROSS APPLY STRING_SPLIT(Tags, ',') " +
           "WHERE TRIM(value) <> '' " +
           "ORDER BY tag")
    List<String> getAllTags();

    /**
     * 根据标签获取文章
     * @param tag 标签
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username as \"author.username\", " +
           "u.Avatar as \"author.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.Tags LIKE CONCAT('%', #{tag}, '%') " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> getPostsByTag(String tag, int offset, int limit);

    /**
     * 搜索文章
     * @param keyword 关键词
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username as \"author.username\", " +
           "u.Avatar as \"author.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Content LIKE CONCAT('%', #{keyword}, '%') " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> searchPosts(String keyword, int offset, int limit);

    /**
     * 获取用户的所有文章
     * @param userId 用户ID
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username as \"author.username\", " +
           "u.Avatar as \"author.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.UserId = #{userId} " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> getPostsByUserId(Integer userId);
    
    /**
     * 获取所有文章（分页）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username, u.Avatar, u.UserId as UserID, " +
           "(SELECT COUNT(*) FROM Comments WHERE PostId = p.PostId) as CommentCount " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "ORDER BY p.CreatedAt DESC " +
           "OFFSET #{offset} ROWS " +
           "FETCH NEXT #{limit} ROWS ONLY")
    @Results({
        @Result(property = "postId", column = "PostId"),
        @Result(property = "userId", column = "UserId"),
        @Result(property = "title", column = "Title"),
        @Result(property = "content", column = "Content"),
        @Result(property = "summary", column = "Summary"),
        @Result(property = "tags", column = "Tags"),
        @Result(property = "createdAt", column = "CreatedAt"),
        @Result(property = "updatedAt", column = "UpdatedAt"),
        @Result(property = "isHidden", column = "IsHidden"),
        @Result(property = "authorName", column = "Username"),
        @Result(property = "authorAvatar", column = "Avatar"),
        @Result(property = "author.userId", column = "UserID"),
        @Result(property = "author.username", column = "Username"),
        @Result(property = "author.avatar", column = "Avatar"),
        @Result(property = "commentCount", column = "CommentCount")
    })
    List<Post> findAllWithUserPaginated(int offset, int limit);
    
    /**
     * 按关键词统计文章总数
     * @param keyword 关键词
     * @return 文章总数
     */
    @Select("SELECT COUNT(*) FROM Posts " +
           "WHERE Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR Content LIKE CONCAT('%', #{keyword}, '%')")
    int getPostCountByKeyword(String keyword);

    @Select("SELECT COUNT(*) FROM Comments WHERE PostId = #{postId}")
    int getCommentCount(Integer postId);
}