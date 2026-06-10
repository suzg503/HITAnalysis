package com.hitanalysis.ai.controller;

import com.hitanalysis.ai.dto.AiQueryDTO;
import com.hitanalysis.ai.service.AiService;
import com.hitanalysis.ai.vo.AiInsightVO;
import com.hitanalysis.ai.vo.AiParseConfirmVO;
import com.hitanalysis.ai.vo.AiPreviewVO;
import com.hitanalysis.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI controller (Text-to-Config implementation)
 *
 * D1: AI only generates Config JSON, SQL is built by backend
 * D2: Insight includes data source and calculation logic
 * D3: Permission check for AI access
 */
@Tag(name = "AI助手", description = "AI自然语言查询、解析确认、预览执行、洞察生成")
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "创建会话", description = "创建新的AI会话")
    @PostMapping("/session")
    public Result<Long> createSession(@Parameter(description = "用户ID") @RequestParam Long userId) {
        Long sessionId = aiService.createSession(userId);
        return Result.success(sessionId);
    }

    @Operation(summary = "提交查询", description = "提交自然语言查询，返回解析结果供确认（D1）")
    @PostMapping("/query")
    public Result<AiParseConfirmVO> submitQuery(@RequestBody AiQueryDTO dto) {
        AiParseConfirmVO result = aiService.submitQuery(dto);
        return Result.success(result);
    }

    @Operation(summary = "获取解析结果", description = "获取解析结果详情（D1-用户必须确认）")
    @GetMapping("/parse/{id}")
    public Result<AiParseConfirmVO> getParseResult(@Parameter(description = "解析ID") @PathVariable Long id) {
        AiParseConfirmVO result = aiService.getParseResult(id);
        return Result.success(result);
    }

    @Operation(summary = "确认执行", description = "确认解析结果并执行查询（D1/D3）")
    @PostMapping("/confirm")
    public Result<AiPreviewVO> confirmAndExecute(
            @Parameter(description = "解析ID") @RequestParam Long parseId,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        AiPreviewVO result = aiService.confirmAndExecute(parseId, userId);
        return Result.success(result);
    }

    @Operation(summary = "追问优化", description = "在预览基础上继续追问优化")
    @PostMapping("/refine")
    public Result<AiParseConfirmVO> refineQuery(
            @Parameter(description = "会话ID") @RequestParam Long sessionId,
            @Parameter(description = "追问内容") @RequestParam String refineText) {
        AiParseConfirmVO result = aiService.refineQuery(sessionId, refineText);
        return Result.success(result);
    }

    @Operation(summary = "生成洞察", description = "基于数据生成AI洞察（D2-含数据来源和计算逻辑）")
    @PostMapping("/insight")
    public Result<AiInsightVO> generateInsight(@Parameter(description = "预览ID") @RequestParam Long previewId) {
        AiInsightVO result = aiService.generateInsight(previewId);
        return Result.success(result);
    }

    @Operation(summary = "检查AI权限", description = "检查用户是否有AI功能权限（D3）")
    @GetMapping("/permission/check")
    public Result<Boolean> checkPermission(@Parameter(description = "用户ID") @RequestParam Long userId) {
        boolean hasPermission = aiService.checkAiPermission(userId);
        return Result.success(hasPermission);
    }

    @Operation(summary = "今日查询次数", description = "获取用户今日AI查询次数")
    @GetMapping("/query-count")
    public Result<Integer> getQueryCount(@Parameter(description = "用户ID") @RequestParam Long userId) {
        int count = aiService.getDailyQueryCount(userId);
        return Result.success(count);
    }
}