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
import com.horzits.business.domain.LyhPracticeCategory;
import com.horzits.business.service.ILyhPracticeCategoryService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/business/lyh/practiceCategory")
@Api(tags = "练习分类")
public class LyhPracticeCategoryController extends BaseController {
    @Autowired
    private ILyhPracticeCategoryService service;

    @ApiOperation(value = "查询练习分类列表", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(LyhPracticeCategory query) {
        startPage();
        List<LyhPracticeCategory> list = service.selectLyhPracticeCategoryList(query);
        return getDataTable(list);
    }

    @GetMapping("/open/list")
    public TableDataInfo openList(LyhPracticeCategory query) {
        startPage();
        query.setStatus("0");
        query.setDelFlag("0");
        List<LyhPracticeCategory> list = service.selectLyhPracticeCategoryList(query);
        return getDataTable(list);
    }

    @ApiOperation(value = "导出练习分类列表", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:export')")
    @Log(title = "练习分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LyhPracticeCategory query) {
        List<LyhPracticeCategory> list = service.selectLyhPracticeCategoryList(query);
        ExcelUtil<LyhPracticeCategory> util = new ExcelUtil<>(LyhPracticeCategory.class);
        util.exportExcel(response, list, "练习分类数据");
    }

    @ApiOperation(value = "获取练习分类详情", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:query')")
    @GetMapping("/{categoryId}")
    public AppRestResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return AppRestResult.success(service.selectLyhPracticeCategoryByCategoryId(categoryId));
    }

    @ApiOperation(value = "新增练习分类", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:add')")
    @Log(title = "练习分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AppRestResult add(@RequestBody LyhPracticeCategory entity) {
        return service.insertLyhPracticeCategory(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "修改练习分类", httpMethod = "PUT", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:edit')")
    @Log(title = "练习分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AppRestResult edit(@RequestBody LyhPracticeCategory entity) {
        return service.updateLyhPracticeCategory(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "删除练习分类", httpMethod = "DELETE", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:practiceCategory:remove')")
    @Log(title = "练习分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AppRestResult remove(@PathVariable Long[] categoryIds) {
        return service.deleteLyhPracticeCategoryByCategoryIds(categoryIds) > 0 ? AppRestResult.success()
                : AppRestResult.error("操作失败");
    }
}
