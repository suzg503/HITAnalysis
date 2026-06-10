package com.hitanalysis.metadata.controller;

import com.hitanalysis.common.constant.SystemConstants;
import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.common.result.Result;
import com.hitanalysis.metadata.entity.BiIndicator;
import com.hitanalysis.metadata.service.IndicatorService;
import com.hitanalysis.metadata.vo.IndicatorVO;
import com.hitanalysis.metadata.vo.IndicatorTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Indicator management controller
 */
@Tag(name = "指标管理", description = "指标查询、树结构、搜索等管理接口")
@RestController
@RequestMapping("/v1/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final IndicatorService indicatorService;

    @Operation(summary = "指标列表", description = "分页查询指标列表")
    @GetMapping
    public Result<PageResult<IndicatorVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "体系ID") @RequestParam(required = false) Long systemId,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long catId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        if (pageSize > SystemConstants.PAGE_SIZE_MAX) {
            pageSize = SystemConstants.PAGE_SIZE_MAX;
        }

        PageResult<IndicatorVO> result = indicatorService.list(pageNum, pageSize, systemId, catId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "指标详情", description = "根据ID获取指标详情")
    @GetMapping("/{id}")
    public Result<IndicatorVO> getById(@Parameter(description = "指标ID") @PathVariable Long id) {
        IndicatorVO result = indicatorService.getById(id);
        return Result.success(result);
    }

    @Operation(summary = "根据代码查询", description = "根据指标代码获取指标")
    @GetMapping("/code/{code}")
    public Result<BiIndicator> getByCode(@Parameter(description = "指标代码") @PathVariable String code) {
        BiIndicator result = indicatorService.getByCode(code);
        return Result.success(result);
    }

    @Operation(summary = "指标树", description = "获取完整的指标树结构（体系-分类-指标）")
    @GetMapping("/tree")
    public Result<List<IndicatorTreeVO>> getTree() {
        List<IndicatorTreeVO> result = indicatorService.getIndicatorTree();
        return Result.success(result);
    }

    @Operation(summary = "搜索指标", description = "根据关键词搜索指标（用于AI匹配）")
    @GetMapping("/search")
    public Result<List<IndicatorVO>> search(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {
        List<IndicatorVO> result = indicatorService.searchByKeyword(keyword, limit);
        return Result.success(result);
    }

    @Operation(summary = "创建指标", description = "创建新指标")
    @PostMapping
    public Result<Long> create(@RequestBody BiIndicator indicator) {
        Long zbId = indicatorService.create(indicator);
        return Result.success(zbId);
    }

    @Operation(summary = "更新指标", description = "更新指标信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "指标ID") @PathVariable Long id,
                               @RequestBody BiIndicator indicator) {
        indicator.setZbId(id);
        indicatorService.update(indicator);
        return Result.success();
    }

    @Operation(summary = "删除指标", description = "软删除指标")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "指标ID") @PathVariable Long id) {
        indicatorService.delete(id);
        return Result.success();
    }

    @Operation(summary = "所有活跃指标", description = "获取所有状态正常的指标（用于RAG）")
    @GetMapping("/all-active")
    public Result<List<BiIndicator>> listAllActive() {
        List<BiIndicator> result = indicatorService.listAllActive();
        return Result.success(result);
    }
}