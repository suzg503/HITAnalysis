package com.hitanalysis.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI report save DTO (D1 decision - Config JSON)
 */
@Data
@Schema(description = "AI报表保存请求")
public class AiReportSaveDTO {

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "配置JSON（AI生成的配置，非SQL）")
    private String configJson;

    @Schema(description = "可见性：private/dept/hospital/all")
    private String visibility;

    @Schema(description = "文件夹ID")
    private Long folderId;

    @Schema(description = "指标ID列表")
    private String zbIds;

    @Schema(description = "状态：saved/published")
    private String status;

    @Schema(description = "原始查询文本")
    private String originalQuery;

    @Schema(description = "数据来源")
    private String dataSource;

    @Schema(description = "计算逻辑")
    private String calcLogic;
}