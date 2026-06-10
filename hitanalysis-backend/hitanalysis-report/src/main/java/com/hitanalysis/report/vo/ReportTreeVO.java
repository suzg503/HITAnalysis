package com.hitanalysis.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Report tree VO
 */
@Data
@Schema(description = "报表树节点")
public class ReportTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "报表代码")
    private String reportCode;

    @Schema(description = "报表层级")
    private Integer reportLevel;

    @Schema(description = "报表URL")
    private String reportUrl;

    @Schema(description = "指标名称列表")
    private String zbNames;

    @Schema(description = "体系名称")
    private String systemName;

    @Schema(description = "分类名称")
    private String catName;

    @Schema(description = "子报表列表")
    private List<ReportTreeVO> children;
}