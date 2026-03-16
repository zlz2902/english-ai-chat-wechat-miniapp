package com.horzits.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.horzits.common.core.domain.AppRestResult;
import com.horzits.common.enums.BusinessType;
import com.horzits.business.domain.LyhCheckin;
import com.horzits.business.service.ILyhCheckinService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import com.horzits.common.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 每日打卡Controller
 * 
 * @author horzits
 * @date 2024-07-02
 */
@RestController
@RequestMapping("/business/lyh/checkin")
@Api(tags = "每日打卡")
public class LyhCheckinController extends BaseController
{
    @Autowired
    private ILyhCheckinService lyhCheckinService;

    /**
     * 查询每日打卡列表
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:list')")
    @GetMapping("/list")
    @ApiOperation("查询每日打卡列表")
    public TableDataInfo list(LyhCheckin lyhCheckin)
    {
        startPage();
        List<LyhCheckin> list = lyhCheckinService.selectLyhCheckinList(lyhCheckin);
        return getDataTable(list);
    }

    /**
     * 导出每日打卡列表
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:export')")
    @Log(title = "每日打卡", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出每日打卡列表")
    public void export(HttpServletResponse response, LyhCheckin lyhCheckin)
    {
        List<LyhCheckin> list = lyhCheckinService.selectLyhCheckinList(lyhCheckin);
        ExcelUtil<LyhCheckin> util = new ExcelUtil<LyhCheckin>(LyhCheckin.class);
        util.exportExcel(response, list, "每日打卡数据");
    }

    /**
     * 获取每日打卡详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:query')")
    @GetMapping(value = "/{checkinId}")
    @ApiOperation("获取每日打卡详细信息")
    public AppRestResult getInfo(@PathVariable("checkinId") Long checkinId)
    {
        return AppRestResult.success(lyhCheckinService.selectLyhCheckinByCheckinId(checkinId));
    }

    /**
     * 新增每日打卡
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:add')")
    @Log(title = "每日打卡", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增每日打卡")
    public AppRestResult add(@RequestBody LyhCheckin lyhCheckin)
    {
        return lyhCheckinService.insertLyhCheckin(lyhCheckin) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    /**
     * 修改每日打卡
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:edit')")
    @Log(title = "每日打卡", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("修改每日打卡")
    public AppRestResult edit(@RequestBody LyhCheckin lyhCheckin)
    {
        return lyhCheckinService.updateLyhCheckin(lyhCheckin) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    /**
     * 删除每日打卡
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:remove')")
    @Log(title = "每日打卡", businessType = BusinessType.DELETE)
	@DeleteMapping("/{checkinIds}")
    @ApiOperation("删除每日打卡")
    public AppRestResult remove(@PathVariable Long[] checkinIds)
    {
        return lyhCheckinService.deleteLyhCheckinByCheckinIds(checkinIds) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }
    
    /**
     * 查询我的打卡列表
     */
    @GetMapping("/my-list")
    @ApiOperation("查询我的打卡列表")
    public AppRestResult myList()
    {
        Long userId = SecurityUtils.getUserId();
        LyhCheckin query = new LyhCheckin();
        query.setUserId(userId);
        List<LyhCheckin> list = lyhCheckinService.selectLyhCheckinList(query);
        return AppRestResult.success(list);
    }

    @Autowired
    private com.horzits.business.service.ILyhStudyStatsService lyhStudyStatsService;

    /**
     * 用户每日打卡
     */
    @PostMapping("/now")
    @ApiOperation("用户每日打卡")
    public AppRestResult checkinNow()
    {
        try {
            Long userId = SecurityUtils.getUserId();
            LyhCheckin checkin = lyhCheckinService.checkin(userId);
            // Sync stats immediately after checkin
            lyhStudyStatsService.syncStatsFromCheckin(userId);
            return AppRestResult.success(checkin);
        } catch (RuntimeException e) {
            return AppRestResult.error(e.getMessage());
        }
    }
    
    /**
     * 查询今日打卡状态
     */
    @GetMapping("/status")
    @ApiOperation("查询今日打卡状态")
    public AppRestResult checkStatus()
    {
        Long userId = SecurityUtils.getUserId();
        boolean hasCheckedIn = lyhCheckinService.hasCheckedInToday(userId);
        return AppRestResult.success(hasCheckedIn);
    }
}
