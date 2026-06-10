package com.hitanalysis.report.controller;

import com.hitanalysis.common.result.Result;
import com.hitanalysis.report.dto.AiReportSaveDTO;
import com.hitanalysis.report.entity.BiAiReport;
import com.hitanalysis.report.entity.BiStandardReport;
import com.hitanalysis.report.service.ReportService;
import com.hitanalysis.report.vo.ReportDataVO;
import com.hitanalysis.report.vo.ReportTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Standard report controller
 */
@Tag(name = "标准报表", description = "标准报表查看、目录树等接口")
@RestController
@RequestMapping("/v1/reports/standard")
@RequiredArgsConstructor
public class StandardReportController {

    private final ReportService reportService;

    @Operation(summary = "报表目录树", description = "获取标准报表目录树结构")
    @GetMapping("/tree")
    public Result<List<ReportTreeVO>> getTree() {
        List<ReportTreeVO> result = reportService.getStandardReportTree();
        return Result.success(result);
    }

    @Operation(summary = "报表详情", description = "根据ID获取报表配置")
    @GetMapping("/{id}")
    public Result<BiStandardReport> getById(@Parameter(description = "报表ID") @PathVariable Long id) {
        BiStandardReport result = reportService.getStandardById(id);
        return Result.success(result);
    }

    @Operation(summary = "查询报表数据", description = "执行报表查询，返回数据（含数据来源D2）")
    @PostMapping("/{id}/data")
    public Result<ReportDataVO> queryData(
            @Parameter(description = "报表ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "时间范围") @RequestParam(required = false) String timeRange) {
        ReportDataVO result = reportService.queryReportData(id, userId, timeRange);
        return Result.success(result);
    }
}