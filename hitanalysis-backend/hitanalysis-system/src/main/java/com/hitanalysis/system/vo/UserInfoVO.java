package com.hitanalysis.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * User info VO (for login response)
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色代码")
    private String roleCode;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "科室代码列表")
    private List<String> deptCodes;

    @Schema(description = "医院ID列表（跨医院权限）")
    private List<Long> hospitalIds;

    @Schema(description = "用户菜单列表")
    private List<MenuTreeVO> menus;
}