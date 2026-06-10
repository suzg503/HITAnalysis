package com.hitanalysis.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Indicator VO (full info with dimension)
 */
@Data
@Schema(description = "指标详情")
public class IndicatorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "指标ID")
    private Long zbId;

    @Schema(description = "指标代码")
    private String zbCode;

    @Schema(description = "指标名称")
    private String zbName;

    @Schema(description = "体系ID")
    private Long systemId;

    @Schema(description = "体系名称")
    private String systemName;

    @Schema(description = "分类ID")
    private Long catId;

    @Schema(description = "分类名称")
    private String catName;

    @Schema(description = "指标意义")
    private String zbMeaning;

    @Schema(description = "指标口径")
    private String zbCaliber;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否实时")
    private Integer isRealTime;

    @Schema(description = "是否有小数")
    private Integer hasDecimal;

    @Schema(description = "配置类型")
    private Integer configType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "事实表")
    private String factTable;

    @Schema(description = "度量字段")
    private String measureField;

    @Schema(description = "维度字段")
    private String dimensionField;

    @Schema(description = "聚合类型")
    private String aggregationType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}