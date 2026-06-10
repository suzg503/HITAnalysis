package com.hitanalysis.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report data VO (query result)
 */
@Data
@Schema(description = "报表数据结果")
public class ReportDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "指标列表")
    private List<String> metrics;

    @Schema(description = "维度列表")
    private List<String> dimensions;

    @Schema(description = "时间范围")
    private String timeRange;

    @Schema(description = "数据行列表")
    private List<Map<String, Object>> dataRows;

    @Schema(description = "数据总量")
    private long total;

    @Schema(description = "图表配置")
    private Map<String, Object> chartConfig;

    @Schema(description = "查询时间")
    private LocalDateTime queryTime;

    @Schema(description = "查询耗时(ms)")
    private long queryDuration;

    // D2 decision - Data source and calculation logic
    @Schema(description = "数据来源")
    private String dataSource;

    @Schema(description = "计算逻辑")
    private String calculationLogic;
}