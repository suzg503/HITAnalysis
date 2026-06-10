package com.hitanalysis.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Indicator tree VO
 */
@Data
@Schema(description = "指标树节点")
public class IndicatorTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "体系ID")
    private Long systemId;

    @Schema(description = "体系代码")
    private String systemCode;

    @Schema(description = "体系名称")
    private String systemName;

    @Schema(description = "分类列表")
    private List<CategoryNode> categories;

    @Data
    @Schema(description = "分类节点")
    public static class CategoryNode implements Serializable {

        @Schema(description = "分类ID")
        private Long catId;

        @Schema(description = "分类代码")
        private String catCode;

        @Schema(description = "分类名称")
        private String catName;

        @Schema(description = "指标列表")
        private List<IndicatorNode> indicators;
    }

    @Data
    @Schema(description = "指标节点")
    public static class IndicatorNode implements Serializable {

        @Schema(description = "指标ID")
        private Long zbId;

        @Schema(description = "指标代码")
        private String zbCode;

        @Schema(description = "指标名称")
        private String zbName;

        @Schema(description = "单位")
        private String unit;
    }
}