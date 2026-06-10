package com.hitanalysis.report.controller;

import com.hitanalysis.common.result.Result;
import com.hitanalysis.report.dto.AiReportSaveDTO;
import com.hitanalysis.report.entity.BiAiReport;
import com.hitanalysis.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI report controller
 */
@Tag(name = "AI报表", description = "AI生成报表的保存、查询、管理接口")
@RestController
@RequestMapping("/v1/reports/ai")
@RequiredArgsConstructor
public class AiReportController {

    private final ReportService reportService;

    @Operation(summary = "用户AI报表列表", description = "获取用户创建的AI报表")
    @GetMapping("/list")
    public Result<List<BiAiReport>> listByUser(@Parameter(description = "用户ID") @RequestParam Long userId) {
        List<BiAiReport> result = reportService.getUserAiReports(userId);
        return Result.success(result);
    }

    @Operation(summary = "AI报表详情", description = "根据ID获取AI报表详情（含Config JSON D1）")
    @GetMapping("/{id}")
    public Result<BiAiReport> getById(@Parameter(description = "报表ID") @PathVariable Long id) {
        BiAiReport result = reportService.getAiById(id);
        return Result.success(result);
    }

    @Operation(summary = "保存AI报表", description = "保存AI生成的报表配置（D1-仅保存JSON配置）")
    @PostMapping("/save")
    public Result<Long> save(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @RequestBody AiReportSaveDTO dto) {
        Long reportId = reportService.saveAiReport(dto, userId);
        return Result.success(reportId);
    }

    @Operation(summary = "更新AI报表", description = "更新AI报表信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "报表ID") @PathVariable Long id,
                               @RequestBody AiReportSaveDTO dto) {
        reportService.updateAiReport(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除AI报表", description = "删除AI报表")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "报表ID") @PathVariable Long id) {
        reportService.deleteAiReport(id);
        return Result.success();
    }

    @Operation(summary = "修改可见性", description = "修改AI报表的可见性范围")
    @PutMapping("/{id}/visibility")
    public Result<Void> changeVisibility(
            @Parameter(description = "报表ID") @PathVariable Long id,
            @Parameter(description = "可见性") @RequestParam String visibility) {
        reportService.changeVisibility(id, visibility);
        return Result.success();
    }
}