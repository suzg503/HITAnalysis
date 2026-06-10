package com.hitanalysis.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI preview VO (preview sandbox result)
 */
@Data
@Schema(description = "AI预览结果")
public class AiPreviewVO {

    @Schema(description = "预览ID")
    private Long previewId;

    @Schema(description = "配置JSON（D1）")
    private String configJson;

    @Schema(description = "数据行")
    private List<Map<String, Object>> dataRows;

    @Schema(description = "数据总量")
    private Long total;

    @Schema(description = "图表类型")
    private String chartType;

    @Schema(description = "图表配置")
    private Map<String, Object> chartConfig;

    @Schema(description = "是否可以继续追问")
    private Boolean canRefine;

    @Schema(description = "查询耗时(ms)")
    private Long queryDuration;
}