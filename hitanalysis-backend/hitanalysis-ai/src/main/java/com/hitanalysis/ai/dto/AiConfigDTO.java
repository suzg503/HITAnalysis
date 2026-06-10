package com.hitanalysis.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI Config DTO (D1 decision - Config JSON, NOT SQL)
 *
 * This is the core of Text-to-Config architecture.
 * AI generates this config, which is then converted to SQL by the backend.
 */
@Data
@Schema(description = "AI配置（D1-只生成JSON配置，不生成SQL）")
public class AiConfigDTO {

    @Schema(description = "意图类型：trend_analysis/comparison/ranking/distribution/detail")
    private String intent;

    @Schema(description = "指标列表")
    private List<MetricConfig> metrics;

    @Schema(description = "维度列表")
    private List<DimensionConfig> dimensions;

    @Schema(description = "时间范围")
    private TimeRangeConfig timeRange;

    @Schema(description = "过滤条件")
    private List<FilterConfig> filters;

    @Schema(description = "计算逻辑")
    private CalculationConfig calculation;

    @Schema(description = "图表类型建议")
    private String suggestedChartType;

    @Schema(description = "置信度")
    private Double confidence;

    @Data
    @Schema(description = "指标配置")
    public static class MetricConfig {
        @Schema(description = "指标ID")
        private Long id;

        @Schema(description = "指标代码")
        private String code;

        @Schema(description = "指标名称")
        private String name;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "聚合类型")
        private String aggregationType;
    }

    @Data
    @Schema(description = "维度配置")
    public static class DimensionConfig {
        @Schema(description = "维度ID")
        private Long id;

        @Schema(description = "维度代码")
        private String code;

        @Schema(description = "维度名称")
        private String name;

        @Schema(description = "层级")
        private String level;
    }

    @Data
    @Schema(description = "时间范围配置")
    public static class TimeRangeConfig {
        @Schema(description = "开始日期")
        private String start;

        @Schema(description = "结束日期")
        private String end;

        @Schema(description = "时间类型：day/month/quarter/year")
        private String type;
    }

    @Data
    @Schema(description = "过滤条件配置")
    public static class FilterConfig {
        @Schema(description = "字段名")
        private String field;

        @Schema(description = "操作符：eq/ne/gt/lt/in")
        private String operator;

        @Schema(description = "值")
        private Object value;
    }

    @Data
    @Schema(description = "计算逻辑配置")
    public static class CalculationConfig {
        @Schema(description = "类型：none/mom/yoy/target")
        private String type;

        @Schema(description = "目标值")
        private Double targetValue;
    }
}