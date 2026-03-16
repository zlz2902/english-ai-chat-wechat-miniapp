package com.horzits.business.controller;

import java.util.List;
import java.util.Map;

import com.horzits.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.horzits.common.core.controller.BaseController;
import com.horzits.common.core.domain.AppRestResult;
import com.horzits.business.domain.LyhCommunity;
import com.horzits.business.service.ILyhCommunityService;

import com.horzits.business.domain.LyhPostComment;
import com.horzits.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * 社区动态Controller
 */
@RestController
@RequestMapping("/business/lyh/community")
public class LyhCommunityController extends BaseController {
    @Autowired
    private ILyhCommunityService lyhCommunityService;

    @GetMapping("/list")
    public TableDataInfo list(LyhCommunity community) {
        startPage();
        // Set current user ID to context for checking likes
        try {
            Long userId = getUserId();
            community.setCurrentUserId(userId);
        } catch (Exception e) {
            // Ignore if not logged in
        }
        List<LyhCommunity> list = lyhCommunityService.selectCommunityList(community);
        return getDataTable(list);
    }

    @PostMapping("/add")
    public AppRestResult add(@RequestBody LyhCommunity community) {
        community.setUserId(getUserId());
        return lyhCommunityService.insertCommunity(community) > 0 ? AppRestResult.success()
                : AppRestResult.error("Failed to add post");
    }

    @PostMapping("/like/{id}")
    public AppRestResult like(@PathVariable Long id) {
        return lyhCommunityService.likeCommunity(id, getUserId()) > 0 ? AppRestResult.success()
                : AppRestResult.error("Failed to like");
    }

    @PostMapping("/comment/{id}")
    public AppRestResult comment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return AppRestResult.error("Content cannot be empty");
        }
        return lyhCommunityService.commentCommunity(id, getUserId(), content) > 0 ? AppRestResult.success()
                : AppRestResult.error("Failed to comment");
    }

    @DeleteMapping("/{ids}")
    public AppRestResult remove(@PathVariable Long[] ids) {
        int rows = lyhCommunityService.deleteCommunityByIds(ids);
        return rows > 0 ? AppRestResult.success() : AppRestResult.error("Failed to delete");
    }

    @GetMapping("/comment/list")
    @com.horzits.common.annotation.Anonymous
    public TableDataInfo commentList(LyhPostComment comment) {
        startPage();
        // Ensure postId is set
        if (comment.getPostId() == null) {
            return getDataTable(new java.util.ArrayList<>());
        }
        List<LyhPostComment> list = lyhCommunityService.selectCommentList(comment);
        return getDataTable(list);
    }

    @DeleteMapping("/comment/{ids}")
    public AppRestResult removeComment(@PathVariable Long[] ids) {
        int rows = lyhCommunityService.deleteCommentByIds(ids);
        return rows > 0 ? AppRestResult.success() : AppRestResult.error("Failed to delete comment");
    }
}
