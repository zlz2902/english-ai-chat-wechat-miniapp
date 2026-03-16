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
import com.horzits.business.domain.LyhDailyWord;
import com.horzits.business.service.ILyhDailyWordService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/business/lyh/dailyWord")
@Api(tags = "每日一词")
public class LyhDailyWordController extends BaseController {
    @Autowired
    private ILyhDailyWordService service;

    @ApiOperation(value = "查询每日一词列表", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:list')")
    @GetMapping("/list")
    public TableDataInfo list(LyhDailyWord query) {
        startPage();
        List<LyhDailyWord> list = service.selectLyhDailyWordList(query);
        return getDataTable(list);
    }

    @ApiOperation(value = "导出每日一词列表", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:export')")
    @Log(title = "每日一词", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LyhDailyWord query) {
        List<LyhDailyWord> list = service.selectLyhDailyWordList(query);
        ExcelUtil<LyhDailyWord> util = new ExcelUtil<>(LyhDailyWord.class);
        util.exportExcel(response, list, "每日一词数据");
    }

    @ApiOperation(value = "获取每日一词详情", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:query')")
    @GetMapping("/{wordId}")
    public AppRestResult getInfo(@PathVariable("wordId") Long wordId) {
        return AppRestResult.success(service.selectLyhDailyWordByWordId(wordId));
    }

    @ApiOperation(value = "获取最新每日一词", httpMethod = "GET", response = AppRestResult.class)
    @GetMapping("/latest")
    public AppRestResult latest() {
        return AppRestResult.success(service.selectLatest());
    }

    @ApiOperation(value = "新增每日一词", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:add')")
    @Log(title = "每日一词", businessType = BusinessType.INSERT)
    @PostMapping
    public AppRestResult add(@RequestBody LyhDailyWord entity) {
        return service.insertLyhDailyWord(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "修改每日一词", httpMethod = "PUT", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:edit')")
    @Log(title = "每日一词", businessType = BusinessType.UPDATE)
    @PutMapping
    public AppRestResult edit(@RequestBody LyhDailyWord entity) {
        return service.updateLyhDailyWord(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "删除每日一词", httpMethod = "DELETE", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:dailyWord:remove')")
    @Log(title = "每日一词", businessType = BusinessType.DELETE)
    @DeleteMapping("/{wordIds}")
    public AppRestResult remove(@PathVariable Long[] wordIds) {
        return service.deleteLyhDailyWordByWordIds(wordIds) > 0 ? AppRestResult.success()
                : AppRestResult.error("操作失败");
    }
}

