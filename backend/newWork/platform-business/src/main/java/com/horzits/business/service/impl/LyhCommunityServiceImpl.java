package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.horzits.business.domain.LyhCommunity;
import com.horzits.business.mapper.LyhCommunityMapper;
import com.horzits.business.service.ILyhCommunityService;
import com.horzits.common.utils.DateUtils;
import java.util.Date;

import com.horzits.business.domain.LyhPostComment;

/**
 * 社区动态Service业务层处理
 */
@Service
public class LyhCommunityServiceImpl implements ILyhCommunityService {

    @Autowired
    private LyhCommunityMapper lyhCommunityMapper;

    // TODO: Should be injected from config, using hardcoded localhost for dev
    private static final String BASE_URL = "http://localhost:8089";

    @Override
    public List<LyhCommunity> selectCommunityList(LyhCommunity community) {
        // Retrieve list from database
        List<LyhCommunity> list = lyhCommunityMapper.selectCommunityList(community);

        // Process each item for display (time formatting, like status)
        for (LyhCommunity item : list) {
            // Format time (e.g., "2h ago")
            if (item.getCreateTime() != null) {
                item.setTimeStr(formatTimeAgo(item.getCreateTime()));
            }

            // Handle avatar URL
            String avatar = item.getAvatar();
            if (avatar == null || avatar.isEmpty()) {
                // Use local asset from Mini Program as default
                item.setAvatar("/assets/profile.jpg");
            } else if (!avatar.startsWith("http") && !avatar.startsWith("/assets")) {
                // Prepend base URL for backend stored images (usually /profile/...)
                item.setAvatar(BASE_URL + avatar);
            }

            // Check if current user liked this post
            if (community.getCurrentUserId() != null) { // community.currentUserId here is used as current user context
                int count = lyhCommunityMapper.checkUserLiked(item.getPostId(), community.getCurrentUserId());
                item.setHasLiked(count > 0);
            }
        }
        return list;
    }

    @Override
    public int insertCommunity(LyhCommunity community) {
        community.setCreateTime(DateUtils.getNowDate());
        community.setLikesCount(0);
        community.setCommentsCount(0);
        community.setDelFlag("0");
        if (community.getContent() == null) {
            community.setContent("");
        }
        return lyhCommunityMapper.insertCommunity(community);
    }

    @Override
    @Transactional
    public int likeCommunity(Long postId, Long userId) {
        // Check if already liked
        int count = lyhCommunityMapper.checkUserLiked(postId, userId);
        if (count > 0) {
            // Unlike
            lyhCommunityMapper.deletePostLike(postId, userId);
            lyhCommunityMapper.decrementLikesCount(postId);
            return 1; // Unliked
        } else {
            // Like
            lyhCommunityMapper.insertPostLike(postId, userId);
            lyhCommunityMapper.incrementLikesCount(postId);
            return 2; // Liked
        }
    }

    @Override
    @Transactional
    public int commentCommunity(Long postId, Long userId, String content) {
        lyhCommunityMapper.insertPostComment(postId, userId, content);
        return lyhCommunityMapper.incrementCommentsCount(postId);
    }
    
    @Override
    public int deleteCommunityByIds(Long[] postIds) {
        return lyhCommunityMapper.deleteCommunityByIds(postIds);
    }

    @Override
    public List<LyhPostComment> selectCommentList(LyhPostComment comment) {
        List<LyhPostComment> list = lyhCommunityMapper.selectCommentList(comment);
        for (LyhPostComment item : list) {
            String avatar = item.getAvatarUrl();
            if (avatar == null || avatar.isEmpty()) {
                item.setAvatarUrl("/assets/profile.jpg");
            } else if (!avatar.startsWith("http") && !avatar.startsWith("/assets")) {
                item.setAvatarUrl(BASE_URL + avatar);
            }
        }
        return list;
    }

    @Override
    public int deleteCommentByIds(Long[] commentIds) {
        // 先查询出这些评论对应的帖子ID，用于扣减计数
        List<LyhPostComment> toDelete = lyhCommunityMapper.selectCommentsByIds(commentIds);
        int rows = lyhCommunityMapper.deleteCommentByIds(commentIds);
        if (rows > 0 && toDelete != null && !toDelete.isEmpty()) {
            java.util.Map<Long, Long> cnt = new java.util.HashMap<>();
            for (LyhPostComment c : toDelete) {
                if (c.getPostId() == null) continue;
                cnt.put(c.getPostId(), cnt.getOrDefault(c.getPostId(), 0L) + 1);
            }
            for (java.util.Map.Entry<Long, Long> e : cnt.entrySet()) {
                lyhCommunityMapper.decrementCommentsCountByPost(e.getKey(), e.getValue().intValue());
            }
        }
        return rows;
    }

    /**
     * Simple helper to format time ago
     */
    private String formatTimeAgo(Date date) {
        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0)
            return days + "d ago";
        if (hours > 0)
            return hours + "h ago";
        if (minutes > 0)
            return minutes + "m ago";
        return "Just now";
    }
}
