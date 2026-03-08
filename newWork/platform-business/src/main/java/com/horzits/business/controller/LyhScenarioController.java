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
import com.horzits.business.domain.LyhScenario;
import com.horzits.business.service.ILyhScenarioService;
import com.horzits.common.utils.poi.ExcelUtil;
import com.horzits.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/business/lyh/scenario")
@Api(tags = "口语练习场景")
public class LyhScenarioController extends BaseController {
    @Autowired
    private ILyhScenarioService service;

    @ApiOperation(value = "查询场景列表", httpMethod = "GET", response = AppRestResult.class)
    @GetMapping("/list")
    public TableDataInfo list(LyhScenario query) {
        startPage();
        List<LyhScenario> list = service.selectLyhScenarioList(query);
        return getDataTable(list);
    }

    /**
     * 小程序开放接口：无需权限的场景列表
     */
    @GetMapping("/open/list")
    public TableDataInfo openList(LyhScenario query) {
        startPage();
        query.setStatus("0");
        query.setDelFlag("0");
        List<LyhScenario> list = service.selectLyhScenarioList(query);
        return getDataTable(list);
    }

    /**
     * 小程序开放接口：无需权限的场景详情（含脚本）
     */
    @GetMapping("/open/{scenarioId}")
    public AppRestResult openGetInfo(@PathVariable("scenarioId") Long scenarioId) {
        LyhScenario scenario = service.selectLyhScenarioByScenarioId(scenarioId);
        if (scenario != null && "0".equals(scenario.getStatus()) && "0".equals(scenario.getDelFlag())) {
            return AppRestResult.success(scenario);
        }
        return AppRestResult.error("场景不存在或已下架");
    }

    @ApiOperation(value = "导出场景列表", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:scenario:export')")
    @Log(title = "口语练习场景", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LyhScenario query) {
        List<LyhScenario> list = service.selectLyhScenarioList(query);
        ExcelUtil<LyhScenario> util = new ExcelUtil<>(LyhScenario.class);
        util.exportExcel(response, list, "场景数据");
    }

    @ApiOperation(value = "获取场景详情", httpMethod = "GET", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:scenario:query')")
    @GetMapping("/{scenarioId}")
    public AppRestResult getInfo(@PathVariable("scenarioId") Long scenarioId) {
        return AppRestResult.success(service.selectLyhScenarioByScenarioId(scenarioId));
    }

    @ApiOperation(value = "新增场景", httpMethod = "POST", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:scenario:add')")
    @Log(title = "口语练习场景", businessType = BusinessType.INSERT)
    @PostMapping
    public AppRestResult add(@RequestBody LyhScenario entity) {
        return service.insertLyhScenario(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "修改场景", httpMethod = "PUT", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:scenario:edit')")
    @Log(title = "口语练习场景", businessType = BusinessType.UPDATE)
    @PutMapping
    public AppRestResult edit(@RequestBody LyhScenario entity) {
        return service.updateLyhScenario(entity) > 0 ? AppRestResult.success() : AppRestResult.error("操作失败");
    }

    @ApiOperation(value = "删除场景", httpMethod = "DELETE", response = AppRestResult.class)
    @PreAuthorize("@ss.hasPermi('lyh:scenario:remove')")
    @Log(title = "口语练习场景", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scenarioIds}")
    public AppRestResult remove(@PathVariable Long[] scenarioIds) {
        return service.deleteLyhScenarioByScenarioIds(scenarioIds) > 0 ? AppRestResult.success()
                : AppRestResult.error("操作失败");
    }
}
