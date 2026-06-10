package com.hitanalysis.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI query DTO (user input)
 */
@Data
@Schema(description = "AI查询请求")
public class AiQueryDTO {

    @Schema(description = "用户自然语言输入")
    private String queryText;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "时间范围（可选）")
    private String timeRange;

    @Schema(description = "医院ID（可选）")
    private Long hospitalId;

    @Schema(description = "科室ID（可选）")
    private Long deptId;
}