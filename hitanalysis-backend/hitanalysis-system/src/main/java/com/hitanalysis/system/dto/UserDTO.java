package com.hitanalysis.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Create/Update user DTO
 */
@Data
@Schema(description = "用户创建/更新请求")
public class UserDTO {

    @Schema(description = "用户ID（更新时必填）")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    @Schema(description = "用户名")
    private String username;

    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    @Schema(description = "密码（创建时必填，更新时可选）")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名最大100个字符")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}