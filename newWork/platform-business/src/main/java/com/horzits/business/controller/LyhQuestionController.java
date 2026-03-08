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
import com.horzits.business.domain.LyhQuestion;
import com.horzits.business.service.ILyhQuestionService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/business/lyh/question")
@Api(tags = "口语题库题目")
public class LyhQuestionController extends BaseController {
    @Autowired
    private ILyhQuestionService service;

    @ApiOperation(value = "查询题目列表", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(LyhQuestion query) {
        startPage();
        List<LyhQuestion> list = service.selectLyhQuestionList(query);
        return getDataTable(list);
    }

    @ApiOperation(value = "导出题目列表", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:export')")
    @Log(title = "口语题库题目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LyhQuestion query) {
        List<LyhQuestion> list = service.selectLyhQuestionList(query);
        ExcelUtil<LyhQuestion> util = new ExcelUtil<>(LyhQuestion.class);
        util.exportExcel(response, list, "题目数据");
    }

    @ApiOperation(value = "获取题目详情", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:query')")
    @GetMapping("/{questionId}")
    public AppRestResult getInfo(@PathVariable("questionId") Long questionId) {
        return AppRestResult.success(service.selectLyhQuestionByQuestionId(questionId));
    }

    @ApiOperation(value = "新增题目", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:add')")
    @Log(title = "口语题库题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AppRestResult add(@RequestBody LyhQuestion entity) {
        return service.insertLyhQuestion(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "修改题目", httpMethod = "PUT", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:edit')")
    @Log(title = "口语题库题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AppRestResult edit(@RequestBody LyhQuestion entity) {
        return service.updateLyhQuestion(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "删除题目", httpMethod = "DELETE", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:question:remove')")
    @Log(title = "口语题库题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{questionIds}")
    public AppRestResult remove(@PathVariable Long[] questionIds) {
        return service.deleteLyhQuestionByQuestionIds(questionIds) > 0 ? AppRestResult.success()
                : AppRestResult.error("操作失败");
    }
}

