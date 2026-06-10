package com.hitanalysis.system.service;

import com.hitanalysis.system.dto.LoginDTO;
import com.hitanalysis.system.vo.LoginVO;
import com.hitanalysis.system.vo.UserInfoVO;

/**
 * Authentication service
 */
public interface AuthService {

    /**
     * Login
     */
    LoginVO login(LoginDTO dto);

    /**
     * Logout
     */
    void logout(Long userId);

    /**
     * Refresh token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * Get current user info
     */
    UserInfoVO getCurrentUser(Long userId);

    /**
     * Build menu tree from flat menu list
     */
    UserInfoVO buildMenuTree(UserInfoVO userInfo);
}