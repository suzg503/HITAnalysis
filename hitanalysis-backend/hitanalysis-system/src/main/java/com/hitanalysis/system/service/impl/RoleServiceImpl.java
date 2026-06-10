package com.hitanalysis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.system.entity.SysMenu;
import com.hitanalysis.system.entity.SysRole;
import com.hitanalysis.system.entity.SysRoleMenu;
import com.hitanalysis.system.mapper.SysMenuMapper;
import com.hitanalysis.system.mapper.SysRoleMapper;
import com.hitanalysis.system.mapper.SysRoleMenuMapper;
import com.hitanalysis.system.service.RoleService;
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
 * Role service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public SysRole getById(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    @Override
    public List<SysRole> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDeleted, 0);
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByDesc(SysRole::getCreateTime);
        return roleMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Long createRole(SysRole role) {
        if (existsByRoleCode(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE);
        }
        roleMapper.insert(role);
        log.info("Role created: roleId={}, roleCode={}", role.getRoleId(), role.getRoleCode());
        return role.getRoleId();
    }

    @Override
    @Transactional
    public void updateRole(SysRole role) {
        SysRole existing = getById(role.getRoleId());
        if (!existing.getRoleCode().equals(role.getRoleCode()) && existsByRoleCode(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE);
        }
        roleMapper.updateById(role);
        log.info("Role updated: roleId={}", role.getRoleId());
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        SysRole role = getById(roleId);
        role.setIsDeleted(1);
        role.setDeletedAt(java.time.LocalDateTime.now());
        roleMapper.updateById(role);
        roleMenuMapper.deleteByRoleId(roleId);
        log.info("Role deleted: roleId={}", roleId);
    }

    @Override
    public List<MenuTreeVO> getRoleMenus(Long roleId) {
        List<SysMenu> menus = menuMapper.selectMenusByRoleId(roleId);
        return buildMenuTree(menus);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);

        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        log.info("Menus assigned to role: roleId={}, menuCount={}", roleId, menuIds.size());
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        wrapper.eq(SysRole::getIsDeleted, 0);
        return roleMapper.selectCount(wrapper) > 0;
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