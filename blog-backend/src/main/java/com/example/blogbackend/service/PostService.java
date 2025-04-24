package com.example.blogbackend.service;

import com.example.blogbackend.entity.Post;
import java.util.List;

/**
 * 文章服务接口
 */
public interface PostService {
    /**
     * 创建新文章
     * @param post 文章信息
     * @return 创建的文章
     */
    Post createPost(Post post);

    /**
     * 获取所有文章
     * @return 文章列表
     */
    List<Post> getAllPosts();

    /**
     * 根据ID获取文章
     * @param postId 文章ID
     * @return 文章信息
     */
    Post getPostById(Integer postId);

    /**
     * 获取用户的所有文章
     * @param userId 用户ID
     * @return 文章列表
     */
    List<Post> getPostsByUserId(Integer userId);

    /**
     * 更新文章
     * @param post 文章信息
     * @return 更新后的文章
     */
    Post updatePost(Post post);

    /**
     * 删除文章
     * @param postId 文章ID
     */
    void deletePost(Integer postId);

    /**
     * 获取所有标签
     * @return 标签列表
     */
    List<String> getAllTags();

    /**
     * 根据标签获取文章
     * @param tags 标签列表
     * @param page 页码
     * @param pageSize 每页数量
     * @return 文章列表
     */
    List<Post> getPostsByTags(List<String> tags, int page, int pageSize);

    /**
     * 根据单个标签获取文章
     * @param tag 标签
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文章列表
     */
    List<Post> getPostsByTag(String tag, int offset, int limit);

    /**
     * 搜索文章
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页数量
     * @return 文章列表
     */
    List<Post> searchPosts(String keyword, int page, int pageSize);

    /**
     * 获取文章总数
     * @param keyword 搜索关键词（如果有）
     * @return 文章总数
     */
    int getPostCount(String keyword);

    /**
     * 获取所有文章（分页）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文章列表
     */
    List<Post> getAllPostsWithPagination(int offset, int limit);

    /**
     * 管理员删除文章（不检查权限）
     * @param postId 文章ID
     */
    void deletePostByAdmin(Integer postId);

    /**
     * 隐藏文章
     * @param postId 文章ID
     */
    void hidePost(Integer postId);

    /**
     * 显示文章
     * @param postId 文章ID
     */
    void showPost(Integer postId);
} 