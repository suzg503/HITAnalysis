package com.hitanalysis.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI parse confirm VO (D1 - user must confirm before execution)
 */
@Data
@Schema(description = "AI解析确认结果（D1-用户必须确认）")
public class AiParseConfirmVO {

    @Schema(description = "解析ID")
    private Long parseId;

    @Schema(description = "原始查询文本")
    private String originalQuery;

    @Schema(description = "识别的意图")
    private String intent;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "识别的指标列表")
    private List<MetricDisplay> metrics;

    @Schema(description = "识别的维度列表")
    private List<DimensionDisplay> dimensions;

    @Schema(description = "识别的时间范围")
    private TimeRangeDisplay timeRange;

    @Schema(description = "识别的过滤条件")
    private List<FilterDisplay> filters;

    @Schema(description = "建议的图表类型")
    private String suggestedChartType;

    @Schema(description = "AI生成的说明")
    private String explanation;

    @Schema(description = "是否需要用户补充")
    private Boolean needsMoreInput;

    @Schema(description = "缺失的信息提示")
    private List<String> missingInfoHints;

    @Data
    @Schema(description = "指标显示")
    public static class MetricDisplay {
        private Long id;
        private String code;
        private String name;
        private String unit;
    }

    @Data
    @Schema(description = "维度显示")
    public static class DimensionDisplay {
        private Long id;
        private String code;
        private String name;
        private String level;
    }

    @Data
    @Schema(description = "时间范围显示")
    public static class TimeRangeDisplay {
        private String start;
        private String end;
        private String displayText;
    }

    @Data
    @Schema(description = "过滤条件显示")
    public static class FilterDisplay {
        private String fieldName;
        private String operatorName;
        private String valueDisplay;
    }
}