package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhCommunity;
import com.horzits.business.domain.LyhPostComment;

/**
 * 社区动态Service接口
 */
public interface ILyhCommunityService {
    /**
     * 查询动态列表
     */
    public List<LyhCommunity> selectCommunityList(LyhCommunity community);

    /**
     * 新增动态
     */
    public int insertCommunity(LyhCommunity community);

    /**
     * 点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    public int likeCommunity(Long postId, Long userId);

    /**
     * 评论
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param content 评论内容
     */
    public int commentCommunity(Long postId, Long userId, String content);
    
    /**
     * 批量删除动态
     * @param postIds
     * @return
     */
    public int deleteCommunityByIds(Long[] postIds);
    
    /**
     * 查询评论列表
     * @param comment
     * @return
     */
    public List<LyhPostComment> selectCommentList(LyhPostComment comment);
    
    /**
     * 批量删除评论
     * @param commentIds
     * @return
     */
    public int deleteCommentByIds(Long[] commentIds);
}
