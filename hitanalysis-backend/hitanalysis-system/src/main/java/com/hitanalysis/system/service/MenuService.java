package com.hitanalysis.system.service;

import com.hitanalysis.system.entity.SysMenu;
import com.hitanalysis.system.vo.MenuTreeVO;

import java.util.List;

/**
 * Menu service
 */
public interface MenuService {

    /**
     * Get menu by ID
     */
    SysMenu getById(Long menuId);

    /**
     * Get all menus as tree
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * Get user menus
     */
    List<MenuTreeVO> getUserMenus(Long userId);

    /**
     * Create menu
     */
    Long createMenu(SysMenu menu);

    /**
     * Update menu
     */
    void updateMenu(SysMenu menu);

    /**
     * Delete menu (soft delete)
     */
    void deleteMenu(Long menuId);

    /**
     * Get child menus
     */
    List<SysMenu> getChildMenus(Long parentId);
}