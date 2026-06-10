package com.hitanalysis.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.system.dto.UserDTO;
import com.hitanalysis.system.entity.SysUser;
import com.hitanalysis.system.vo.UserInfoVO;

/**
 * User service
 */
public interface UserService {

    /**
     * Get user by ID
     */
    SysUser getById(Long userId);

    /**
     * List users with pagination
     */
    PageResult<UserInfoVO> listUsers(int pageNum, int pageSize, String username, Integer status);

    /**
     * Create user
     */
    Long createUser(UserDTO dto);

    /**
     * Update user
     */
    void updateUser(UserDTO dto);

    /**
     * Delete user (soft delete)
     */
    void deleteUser(Long userId);

    /**
     * Change user status
     */
    void changeStatus(Long userId, Integer status);

    /**
     * Check username exists
     */
    boolean existsByUsername(String username);
}