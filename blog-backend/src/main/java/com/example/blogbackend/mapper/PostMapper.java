package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    /**
     * 更新文章
     * @param post 文章信息
     * @return 更新的行数
     */
    @Update("UPDATE Posts SET " +
            "Title = #{title}, " +
            "Content = #{content}, " +
            "Summary = #{summary}, " +
            "UpdatedAt = #{updatedAt}, " +
            "Tags = #{tags}, " +
            "IsHidden = #{isHidden}, " +
            "IsDeleted = #{isDeleted}, " +
            "AuthorName = #{authorName} " +
            "WHERE PostId = #{postId}")
    int updateById(Post post);

    /**
     * 查找文章及其作者信息
     * @param postId 文章ID
     * @return 文章及作者信息
     */
    @Select("SELECT p.PostId, p.Title, p.IsDeleted, p.CreatedAt, p.Tags, " +
           "CASE WHEN p.IsDeleted = 1 THEN '已删除' ELSE p.Content END as Content, " +
           "u.Username, u.Avatar, u.UserId as UserID, " +
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
        @Result(property = "commentCount", column = "CommentCount"),
        @Result(property = "isDeleted", column = "IsDeleted")
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
           "WHERE NOT (p.IsDeleted = 1 OR p.IsHidden = 1) " +
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
     * 搜索文章（简化版）
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username, u.Avatar, u.UserId as UserID, " +
           "(SELECT COUNT(*) FROM Comments WHERE PostId = p.PostId) as CommentCount " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE NOT (p.IsDeleted = 1 OR p.IsHidden = 1) AND (" +
           "   p.Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Content LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Tags LIKE CONCAT('%', #{keyword}, '%') " +
           ")" +
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
        @Result(property = "commentCount", column = "CommentCount"),
        @Result(property = "isDeleted", column = "IsDeleted")
    })
    List<Post> searchPosts(String keyword, int offset, int limit);

    /**
     * 获取所有标签
     * @return 标签列表
     */
    @Select("WITH TagData AS (" +
           "    SELECT DISTINCT Tags " +
           "    FROM Posts " +
           "    WHERE Tags IS NOT NULL AND Tags <> ''" +
           "), " +
           "SplitTags AS (" +
           "    SELECT value AS tag " +
           "    FROM TagData " +
           "    CROSS APPLY STRING_SPLIT(Tags, ',') " +
           ") " +
           "SELECT DISTINCT TRIM(tag) as tag " +
           "FROM SplitTags " +
           "WHERE TRIM(tag) <> '' " +
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
           "WHERE ',' + p.Tags + ',' LIKE '%,' + #{tag} + ',%' " +
           "ORDER BY p.CreatedAt DESC " +
           "OFFSET #{offset} ROWS " +
           "FETCH NEXT #{limit} ROWS ONLY")
    List<Post> getPostsByTag(String tag, int offset, int limit);

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
           "WHERE NOT (p.IsDeleted = 1 OR p.IsHidden = 1) " +
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
           "WHERE NOT (IsDeleted = 1 OR IsHidden = 1) AND (" +
           "   Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR Content LIKE CONCAT('%', #{keyword}, '%')" +
           ")")
    int getPostCountByKeyword(String keyword);

    @Select("SELECT COUNT(*) FROM Comments WHERE PostId = #{postId}")
    int getCommentCount(Integer postId);

    /**
     * 删除文章的所有评论
     * @param postId 文章ID
     */
    @Select("DELETE FROM Comments WHERE PostId = #{postId}")
    void deleteCommentsByPostId(Integer postId);

    /**
     * 删除文章（软删除）
     * @param postId 文章ID
     */
    @Select("UPDATE Posts SET IsDeleted = 1, UpdatedAt = GETDATE() WHERE PostId = #{postId}")
    void deletePost(Integer postId);

    /**
     * 更新文章的隐藏状态
     * @param postId 文章ID
     * @param isHidden 是否隐藏
     * @return 更新的行数
     */
    @Select("UPDATE Posts SET IsHidden = #{isHidden} WHERE PostId = #{postId}")
    int updatePostHiddenStatus(Integer postId, Boolean isHidden);

    /**
     * 管理员获取所有文章（分页）- 包括已删除和隐藏的文章
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
        @Result(property = "isDeleted", column = "IsDeleted"),
        @Result(property = "authorName", column = "Username"),
        @Result(property = "authorAvatar", column = "Avatar"),
        @Result(property = "author.userId", column = "UserID"),
        @Result(property = "author.username", column = "Username"),
        @Result(property = "author.avatar", column = "Avatar"),
        @Result(property = "commentCount", column = "CommentCount")
    })
    List<Post> findAllWithUserPaginatedForAdmin(int offset, int limit);

    /**
     * 管理员搜索文章（包括已删除和隐藏的文章）
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文章列表
     */
    @Select("SELECT p.*, u.Username, u.Avatar, u.UserId as UserID, " +
           "(SELECT COUNT(*) FROM Comments WHERE PostId = p.PostId) as CommentCount " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Content LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR p.Tags LIKE CONCAT('%', #{keyword}, '%') " +
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
        @Result(property = "isDeleted", column = "IsDeleted"),
        @Result(property = "authorName", column = "Username"),
        @Result(property = "authorAvatar", column = "Avatar"),
        @Result(property = "author.userId", column = "UserID"),
        @Result(property = "author.username", column = "Username"),
        @Result(property = "author.avatar", column = "Avatar"),
        @Result(property = "commentCount", column = "CommentCount")
    })
    List<Post> searchPostsForAdmin(String keyword, int offset, int limit);

    /**
     * 管理员统计所有文章总数（包括已删除和隐藏的）
     * @param keyword 关键词
     * @return 文章总数
     */
    @Select("SELECT COUNT(*) FROM Posts " +
           "WHERE Title LIKE CONCAT('%', #{keyword}, '%') " +
           "   OR Content LIKE CONCAT('%', #{keyword}, '%')")
    int getPostCountByKeywordForAdmin(String keyword);
}