package com.hitanalysis.system.controller;

import com.hitanalysis.common.constant.SystemConstants;
import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.common.result.Result;
import com.hitanalysis.system.dto.UserDTO;
import com.hitanalysis.system.entity.SysUser;
import com.hitanalysis.system.service.UserService;
import com.hitanalysis.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller
 */
@Tag(name = "用户管理", description = "用户增删改查等管理接口")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @GetMapping
    public Result<PageResult<UserInfoVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        if (pageSize > SystemConstants.PAGE_SIZE_MAX) {
            pageSize = SystemConstants.PAGE_SIZE_MAX;
        }

        PageResult<UserInfoVO> result = userService.listUsers(pageNum, pageSize, username, status);
        return Result.success(result);
    }

    @Operation(summary = "用户详情", description = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        SysUser user = userService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "用户ID") @PathVariable Long id,
                               @Valid @RequestBody UserDTO dto) {
        dto.setUserId(id);
        userService.updateUser(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "软删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "修改状态", description = "启用或禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@Parameter(description = "用户ID") @PathVariable Long id,
                                      @Parameter(description = "状态：0禁用 1启用") @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return Result.success();
    }
}