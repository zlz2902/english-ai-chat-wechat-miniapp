package com.horzits.business.mapper;

import java.util.List;
import com.horzits.business.domain.LyhCommunity;
import com.horzits.business.domain.LyhPostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区动态Mapper接口
 * 
 * @author horzits
 * @date 2024-07-02
 */
@Mapper
public interface LyhCommunityMapper {
    /**
     * 查询社区动态列表
     * 
     * @param lyhCommunity 社区动态
     * @return 社区动态集合
     */
    public List<LyhCommunity> selectCommunityList(LyhCommunity lyhCommunity);

    /**
     * 新增社区动态
     * 
     * @param lyhCommunity 社区动态
     * @return 结果
     */
    public int insertCommunity(LyhCommunity lyhCommunity);

    /**
     * 修改社区动态
     * 
     * @param lyhCommunity 社区动态
     * @return 结果
     */
    public int updateCommunity(LyhCommunity lyhCommunity);

    /**
     * 删除社区动态
     * 
     * @param postId 社区动态ID
     * @return 结果
     */
    public int deleteCommunityById(Long postId);

    /**
     * 批量删除社区动态
     * 
     * @param postIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteCommunityByIds(Long[] postIds);

    /**
     * 检查用户是否已点赞
     */
    public int checkUserLiked(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 添加点赞记录
     */
    public int insertPostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 删除点赞记录
     */
    public int deletePostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 增加点赞数
     */
    public int incrementLikesCount(Long postId);

    /**
     * 减少点赞数
     */
    public int decrementLikesCount(Long postId);

    /**
     * 添加评论记录
     */
    public int insertPostComment(@Param("postId") Long postId, @Param("userId") Long userId, @Param("content") String content);

    /**
     * 增加评论数
     */
    public int incrementCommentsCount(Long postId);

    /**
     * 查询帖子评论列表
     * @param comment 评论查询条件
     * @return
     */
    public List<LyhPostComment> selectCommentList(LyhPostComment comment);
    
    /**
     * 删除评论
     * @param commentIds
     * @return
     */
    public int deleteCommentByIds(Long[] commentIds);
    
    /**
     * 查询评论（按ID集合）
     * @param commentIds
     * @return
     */
    public List<LyhPostComment> selectCommentsByIds(Long[] commentIds);
    
    /**
     * 按帖子扣减评论数
     * @param postId
     * @param delta
     * @return
     */
    public int decrementCommentsCountByPost(@Param("postId") Long postId, @Param("delta") int delta);
}
