package com.hitanalysis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.system.entity.SysMenu;
import com.hitanalysis.system.mapper.SysMenuMapper;
import com.hitanalysis.system.service.MenuService;
import com.hitanalysis.system.vo.MenuTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Menu service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public SysMenu getById(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null || menu.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        return menu;
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getIsDeleted, 0);
        wrapper.eq(SysMenu::getStatus, 1);
        wrapper.orderByAsc(SysMenu::getMenuLevel);
        wrapper.orderByAsc(SysMenu::getSortNum);

        List<SysMenu> menus = menuMapper.selectList(wrapper);
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuTreeVO> getUserMenus(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus);
    }

    @Override
    @Transactional
    public Long createMenu(SysMenu menu) {
        menuMapper.insert(menu);
        log.info("Menu created: menuId={}, menuName={}", menu.getMenuId(), menu.getMenuName());
        return menu.getMenuId();
    }

    @Override
    @Transactional
    public void updateMenu(SysMenu menu) {
        getById(menu.getMenuId());
        menuMapper.updateById(menu);
        log.info("Menu updated: menuId={}", menu.getMenuId());
    }

    @Override
    @Transactional
    public void deleteMenu(Long menuId) {
        List<SysMenu> children = getChildMenus(menuId);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
        }

        SysMenu menu = getById(menuId);
        menu.setIsDeleted(1);
        menu.setDeletedAt(java.time.LocalDateTime.now());
        menuMapper.updateById(menu);
        log.info("Menu deleted: menuId={}", menuId);
    }

    @Override
    public List<SysMenu> getChildMenus(Long parentId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, parentId);
        wrapper.eq(SysMenu::getIsDeleted, 0);
        return menuMapper.selectList(wrapper);
    }

    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        List<MenuTreeVO> voList = menus.stream()
                .map(m -> {
                    MenuTreeVO vo = new MenuTreeVO();
                    vo.setMenuId(m.getMenuId());
                    vo.setParentId(m.getParentId());
                    vo.setMenuName(m.getMenuName());
                    vo.setMenuCode(m.getMenuCode());
                    vo.setMenuLevel(m.getMenuLevel());
                    vo.setLinkUrl(m.getLinkUrl());
                    vo.setSortNum(m.getSortNum());
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toList());

        Map<Long, MenuTreeVO> voMap = voList.stream()
                .collect(Collectors.toMap(MenuTreeVO::getMenuId, v -> v));

        List<MenuTreeVO> rootMenus = new ArrayList<>();
        for (MenuTreeVO vo : voList) {
            if (vo.getParentId() == 0) {
                rootMenus.add(vo);
            } else {
                MenuTreeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        return rootMenus;
    }
}