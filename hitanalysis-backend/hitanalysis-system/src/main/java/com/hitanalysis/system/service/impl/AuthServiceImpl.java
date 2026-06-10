package com.hitanalysis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.constant.SystemConstants;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.common.utils.JwtUtils;
import com.hitanalysis.common.utils.PasswordUtils;
import com.hitanalysis.system.dto.LoginDTO;
import com.hitanalysis.system.entity.SysMenu;
import com.hitanalysis.system.entity.SysRole;
import com.hitanalysis.system.entity.SysUser;
import com.hitanalysis.system.mapper.SysMenuMapper;
import com.hitanalysis.system.mapper.SysRoleMapper;
import com.hitanalysis.system.mapper.SysUserMapper;
import com.hitanalysis.system.service.AuthService;
import com.hitanalysis.system.vo.LoginVO;
import com.hitanalysis.system.vo.MenuTreeVO;
import com.hitanalysis.system.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Authentication service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final JwtUtils jwtUtils;
    private final PasswordUtils passwordUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public LoginVO login(LoginDTO dto) {
        // Find user by username
        SysUser user = userMapper.selectByUsernameWithRole(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);
        }

        // Check status
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.LOGIN_USER_DISABLED);
        }

        // Verify password
        if (!passwordUtils.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_ERROR);
        }

        // Generate tokens
        String accessToken = jwtUtils.generateToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUserId());

        // Cache token in Redis (D3: permission cache)
        String tokenKey = SystemConstants.CACHE_TOKEN_KEY + user.getUserId();
        redisTemplate.opsForValue().set(tokenKey, accessToken, SystemConstants.SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // Build user info
        UserInfoVO userInfo = buildUserInfo(user);

        // Build response
        LoginVO response = new LoginVO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(86400000L); // 24 hours
        response.setUserInfo(userInfo);

        log.info("User login success: userId={}, username={}", user.getUserId(), user.getUsername());
        return response;
    }

    @Override
    public void logout(Long userId) {
        String tokenKey = SystemConstants.CACHE_TOKEN_KEY + userId;
        redisTemplate.delete(tokenKey);
        log.info("User logout: userId={}", userId);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);
        }

        // Generate new tokens
        String newAccessToken = jwtUtils.generateToken(user.getUserId(), user.getUsername());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getUserId());

        // Update cache
        String tokenKey = SystemConstants.CACHE_TOKEN_KEY + userId;
        redisTemplate.opsForValue().set(tokenKey, newAccessToken, SystemConstants.SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        UserInfoVO userInfo = buildUserInfo(user);

        LoginVO response = new LoginVO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(86400000L);
        response.setUserInfo(userInfo);

        return response;
    }

    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return buildUserInfo(user);
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRoleId(user.getRoleId());
        userInfo.setHospitalId(user.getHospitalId());

        // Get role info
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role != null) {
            userInfo.setRoleName(role.getRoleName());
            userInfo.setRoleCode(role.getRoleCode());
        }

        // Get user departments (for D3 permission filtering)
        List<String> deptCodes = userMapper.selectUserDeptCodes(user.getUserId());
        userInfo.setDeptCodes(deptCodes);

        // Get user hospitals (for cross-hospital permission D3)
        List<Long> hospitalIds = userMapper.selectUserHospitalIds(user.getUserId());
        userInfo.setHospitalIds(hospitalIds);

        // Get user menus
        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getUserId());
        List<MenuTreeVO> menuTree = buildMenuTreeList(menus);
        userInfo.setMenus(menuTree);

        return userInfo;
    }

    private List<MenuTreeVO> buildMenuTreeList(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // Convert to VO
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

        // Build tree
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

    @Override
    public UserInfoVO buildMenuTree(UserInfoVO userInfo) {
        return userInfo;
    }
}