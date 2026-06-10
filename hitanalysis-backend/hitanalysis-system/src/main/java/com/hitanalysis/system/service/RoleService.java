package com.hitanalysis.system.service;

import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.system.entity.SysRole;
import com.hitanalysis.system.vo.MenuTreeVO;

import java.util.List;

/**
 * Role service
 */
public interface RoleService {

    /**
     * Get role by ID
     */
    SysRole getById(Long roleId);

    /**
     * List all roles
     */
    List<SysRole> listAll();

    /**
     * Create role
     */
    Long createRole(SysRole role);

    /**
     * Update role
     */
    void updateRole(SysRole role);

    /**
     * Delete role (soft delete)
     */
    void deleteRole(Long roleId);

    /**
     * Get menus assigned to role
     */
    List<MenuTreeVO> getRoleMenus(Long roleId);

    /**
     * Assign menus to role
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * Check role code exists
     */
    boolean existsByRoleCode(String roleCode);
}