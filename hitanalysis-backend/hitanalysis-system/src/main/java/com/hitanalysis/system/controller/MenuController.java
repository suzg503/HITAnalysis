package com.hitanalysis.system.controller;

import com.hitanalysis.common.result.Result;
import com.hitanalysis.system.entity.SysMenu;
import com.hitanalysis.system.service.MenuService;
import com.hitanalysis.system.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu management controller
 */
@Tag(name = "菜单管理", description = "菜单增删改查、菜单树等管理接口")
@RestController
@RequestMapping("/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单树", description = "获取完整菜单树结构")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> getTree() {
        List<MenuTreeVO> tree = menuService.getMenuTree();
        return Result.success(tree);
    }

    @Operation(summary = "用户菜单", description = "获取当前用户的菜单树")
    @GetMapping("/user")
    public Result<List<MenuTreeVO>> getUserMenus(@Parameter(description = "用户ID") @RequestParam Long userId) {
        List<MenuTreeVO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }

    @Operation(summary = "菜单详情", description = "根据ID获取菜单详情")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@Parameter(description = "菜单ID") @PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        return Result.success(menu);
    }

    @Operation(summary = "创建菜单", description = "创建新菜单")
    @PostMapping
    public Result<Long> create(@RequestBody SysMenu menu) {
        Long menuId = menuService.createMenu(menu);
        return Result.success(menuId);
    }

    @Operation(summary = "更新菜单", description = "更新菜单信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "菜单ID") @PathVariable Long id,
                               @RequestBody SysMenu menu) {
        menu.setMenuId(id);
        menuService.updateMenu(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单", description = "软删除菜单（需先删除子菜单）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}