package com.hitanalysis.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Menu tree VO
 */
@Data
@Schema(description = "菜单树节点")
public class MenuTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单代码")
    private String menuCode;

    @Schema(description = "菜单层级")
    private Integer menuLevel;

    @Schema(description = "链接地址")
    private String linkUrl;

    @Schema(description = "排序")
    private Integer sortNum;

    @Schema(description = "子菜单列表")
    private List<MenuTreeVO> children;
}