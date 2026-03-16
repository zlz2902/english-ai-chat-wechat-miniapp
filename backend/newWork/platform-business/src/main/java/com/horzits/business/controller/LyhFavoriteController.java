package com.horzits.business.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.horzits.business.service.ILyhFavoriteService;
import com.horzits.common.core.controller.BaseController;
import com.horzits.common.core.domain.AppRestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/business/lyh/favorite")
@Api(tags = "收藏夹")
public class LyhFavoriteController extends BaseController {

    @Autowired
    private ILyhFavoriteService service;

    @Autowired
    private com.horzits.business.service.ILyhQuestionService questionService;

    @ApiOperation(value = "收藏列表(返回targetId列表)", httpMethod = "GET", response = AppRestResult.class)
    @GetMapping("/list")
    public AppRestResult list(@RequestParam("bizType") String bizType) {
        Long userId = getUserId();
        List<Long> targetIds = service.listTargets(userId, bizType);
        return AppRestResult.success(targetIds);
    }

    @ApiOperation(value = "我的收藏题目列表", httpMethod = "GET", response = AppRestResult.class)
    @GetMapping("/questions")
    public AppRestResult listQuestions() {
        Long userId = getUserId();
        List<com.horzits.business.domain.LyhQuestion> list = questionService.selectFavoritedQuestions(userId);
        return AppRestResult.success(list);
    }

    @ApiOperation(value = "检查是否已收藏", httpMethod = "GET", response = AppRestResult.class)
    @GetMapping("/check")
    public AppRestResult check(@RequestParam("bizType") String bizType, @RequestParam("targetId") Long targetId) {
        Long userId = getUserId();
        boolean favorited = service.isFavorited(userId, bizType, targetId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return AppRestResult.success(data);
    }

    @ApiOperation(value = "切换收藏状态", httpMethod = "POST", response = AppRestResult.class)
    @PostMapping("/toggle")
    public AppRestResult toggle(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        String bizType = String.valueOf(body.getOrDefault("bizType", "1"));
        Long targetId = ((Number) body.getOrDefault("targetId", 0)).longValue();
        boolean favorited = service.toggle(userId, bizType, targetId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return AppRestResult.success(data);
    }
}
