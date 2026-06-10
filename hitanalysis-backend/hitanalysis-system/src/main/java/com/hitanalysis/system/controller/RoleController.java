package com.hitanalysis.system.controller;

import com.hitanalysis.common.result.Result;
import com.hitanalysis.system.entity.SysRole;
import com.hitanalysis.system.service.RoleService;
import com.hitanalysis.system.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role management controller
 */
@Tag(name = "角色管理", description = "角色增删改查、菜单分配等管理接口")
@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表", description = "获取所有角色列表")
    @GetMapping
    public Result<List<SysRole>> list() {
        List<SysRole> roles = roleService.listAll();
        return Result.success(roles);
    }

    @Operation(summary = "角色详情", description = "根据ID获取角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@Parameter(description = "角色ID") @PathVariable Long id) {
        SysRole role = roleService.getById(id);
        return Result.success(role);
    }

    @Operation(summary = "创建角色", description = "创建新角色")
    @PostMapping
    public Result<Long> create(@RequestBody SysRole role) {
        Long roleId = roleService.createRole(role);
        return Result.success(roleId);
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "角色ID") @PathVariable Long id,
                               @RequestBody SysRole role) {
        role.setRoleId(id);
        roleService.updateRole(role);
        return Result.success();
    }

    @Operation(summary = "删除角色", description = "软删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "获取角色菜单", description = "获取角色已分配的菜单")
    @GetMapping("/{id}/menus")
    public Result<List<MenuTreeVO>> getMenus(@Parameter(description = "角色ID") @PathVariable Long id) {
        List<MenuTreeVO> menus = roleService.getRoleMenus(id);
        return Result.success(menus);
    }

    @Operation(summary = "分配菜单", description = "为角色分配菜单权限")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@Parameter(description = "角色ID") @PathVariable Long id,
                                     @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
}