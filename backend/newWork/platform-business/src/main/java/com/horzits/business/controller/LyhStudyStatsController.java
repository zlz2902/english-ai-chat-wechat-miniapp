package com.horzits.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.horzits.common.core.domain.AppRestResult;
import com.horzits.common.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.horzits.common.annotation.Log;
import com.horzits.common.core.controller.BaseController;
import com.horzits.common.core.domain.AjaxResult;
import com.horzits.common.enums.BusinessType;
import com.horzits.business.domain.LyhStudyStats;
import com.horzits.business.service.ILyhStudyStatsService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import com.horzits.business.mapper.LyhCheckinMapper;
import com.horzits.system.mapper.SysUserMapper;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;

/**
 * 学习统计Controller
 */
@Api(tags = "学习统计管理")
@RestController
@RequestMapping("/business/lyh/stats")
public class LyhStudyStatsController extends BaseController
{
    @Autowired
    private ILyhStudyStatsService lyhStudyStatsService;
    
    @Autowired
    private LyhCheckinMapper checkinMapper;
    
    @Autowired
    private SysUserMapper userMapper;

    /**
     * 获取管理端首页总览数据
     */
    @ApiOperation("获取管理端首页总览数据")
    @GetMapping("/overview")
    public AppRestResult getOverview() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(now);
        
        int activeToday = checkinMapper.selectCountByDate(today);
        int newUsersToday = userMapper.selectCountByCreateTime(today);
        
        Map<String, Object> data = new HashMap<>();
        data.put("activeToday", activeToday);
        data.put("newUsersToday", newUsersToday);
        
        return AppRestResult.success(data);
    }

    /**
     * 查询我的学习统计
     */
    @ApiOperation("查询我的学习统计")
    @GetMapping("/my")
    public AppRestResult getMyStats()
    {
        Long userId = SecurityUtils.getUserId();
        // Sync stats before returning to ensure data consistency
        lyhStudyStatsService.syncStatsFromCheckin(userId);
        
        LyhStudyStats stats = lyhStudyStatsService.selectLyhStudyStatsByUserId(userId);
        return AppRestResult.success(stats);
    }

    /**
     * 查询学习统计列表
     */
    @PreAuthorize("@ss.hasPermi('business:stats:list')")
    @GetMapping("/list")
    public TableDataInfo list(LyhStudyStats lyhStudyStats)
    {
        startPage();
        List<LyhStudyStats> list = lyhStudyStatsService.selectLyhStudyStatsList(lyhStudyStats);
        return getDataTable(list);
    }

    /**
     * 导出学习统计列表
     */
    @PreAuthorize("@ss.hasPermi('business:stats:export')")
    @Log(title = "学习统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LyhStudyStats lyhStudyStats)
    {
        List<LyhStudyStats> list = lyhStudyStatsService.selectLyhStudyStatsList(lyhStudyStats);
        ExcelUtil<LyhStudyStats> util = new ExcelUtil<LyhStudyStats>(LyhStudyStats.class);
        util.exportExcel(response, list, "学习统计数据");
    }

    /**
     * 获取学习统计详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:stats:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return AjaxResult.success(lyhStudyStatsService.selectLyhStudyStatsByUserId(userId));
    }

    /**
     * 新增学习统计
     */
    @PreAuthorize("@ss.hasPermi('business:stats:add')")
    @Log(title = "学习统计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LyhStudyStats lyhStudyStats)
    {
        return toAjax(lyhStudyStatsService.insertLyhStudyStats(lyhStudyStats));
    }

    /**
     * 修改学习统计
     */
    @PreAuthorize("@ss.hasPermi('business:stats:edit')")
    @Log(title = "学习统计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LyhStudyStats lyhStudyStats)
    {
        return toAjax(lyhStudyStatsService.updateLyhStudyStats(lyhStudyStats));
    }

    /**
     * 删除学习统计
     */
    @PreAuthorize("@ss.hasPermi('business:stats:remove')")
    @Log(title = "学习统计", businessType = BusinessType.DELETE)
	@DeleteMapping(value = "/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(lyhStudyStatsService.deleteLyhStudyStatsByUserIds(userIds));
    }
}
