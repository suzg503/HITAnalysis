package com.hitanalysis.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI insight VO (D2 - insight based on data, with source and logic)
 */
@Data
@Schema(description = "AI洞察结果（D2-基于数据生成，含来源和逻辑）")
public class AiInsightVO {

    @Schema(description = "洞察ID")
    private Long insightId;

    @Schema(description = "洞察标题")
    private String title;

    @Schema(description = "洞察内容")
    private String content;

    @Schema(description = "关键发现")
    private List<String> keyFindings;

    @Schema(description = "建议行动")
    private List<String> recommendations;

    @Schema(description = "异常预警")
    private List<AlertInfo> alerts;

    // D2 decision fields
    @Schema(description = "数据来源（D2必须）")
    private DataSourceInfo dataSource;

    @Schema(description = "计算逻辑（D2必须）")
    private CalculationLogicInfo calculationLogic;

    @Schema(description = "生成时间")
    private String generatedAt;

    @Data
    @Schema(description = "预警信息")
    public static class AlertInfo {
        private String type;
        private String message;
        private Double threshold;
        private Double actualValue;
    }

    @Data
    @Schema(description = "数据来源信息（D2）")
    public static class DataSourceInfo {
        @Schema(description = "数据表名")
        private String tableName;

        @Schema(description = "数据来源系统")
        private String sourceSystem;

        @Schema(description = "时间范围")
        private String timeRange;

        @Schema(description = "数据量")
        private Long dataCount;

        @Schema(description = "最后更新时间")
        private String lastUpdated;
    }

    @Data
    @Schema(description = "计算逻辑信息（D2）")
    public static class CalculationLogicInfo {
        @Schema(description = "指标名称")
        private String metricName;

        @Schema(description = "计算公式")
        private String formula;

        @Schema(description = "聚合方式")
        private String aggregationType;

        @Schema(description = "同比/环比说明")
        private String comparisonType;
    }
}