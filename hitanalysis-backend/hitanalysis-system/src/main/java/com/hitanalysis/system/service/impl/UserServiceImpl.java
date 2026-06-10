package com.hitanalysis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.constant.StatusEnum;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.common.utils.PasswordUtils;
import com.hitanalysis.system.dto.UserDTO;
import com.hitanalysis.system.entity.SysRole;
import com.hitanalysis.system.entity.SysUser;
import com.hitanalysis.system.mapper.SysRoleMapper;
import com.hitanalysis.system.mapper.SysUserMapper;
import com.hitanalysis.system.service.UserService;
import com.hitanalysis.system.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordUtils passwordUtils;

    @Override
    public SysUser getById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public PageResult<UserInfoVO> listUsers(int pageNum, int pageSize, String username, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsDeleted, 0);
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<UserInfoVO> voList = page.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public Long createUser(UserDTO dto) {
        if (existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRoleId(dto.getRoleId());
        user.setHospitalId(dto.getHospitalId());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ENABLE.getCode());
        user.setDeptOption(1);

        userMapper.insert(user);
        log.info("User created: userId={}, username={}", user.getUserId(), user.getUsername());
        return user.getUserId();
    }

    @Override
    @Transactional
    public void updateUser(UserDTO dto) {
        SysUser existing = getById(dto.getUserId());

        if (!existing.getUsername().equals(dto.getUsername()) && existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        existing.setUsername(dto.getUsername());
        existing.setRealName(dto.getRealName());
        existing.setRoleId(dto.getRoleId());
        existing.setHospitalId(dto.getHospitalId());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPasswordHash(passwordUtils.encode(dto.getPassword()));
        }

        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        userMapper.updateById(existing);
        log.info("User updated: userId={}", dto.getUserId());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        SysUser user = getById(userId);
        user.setIsDeleted(1);
        user.setDeletedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User deleted (soft): userId={}", userId);
    }

    @Override
    @Transactional
    public void changeStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("User status changed: userId={}, status={}", userId, status);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        wrapper.eq(SysUser::getIsDeleted, 0);
        return userMapper.selectCount(wrapper) > 0;
    }

    private UserInfoVO convertToVo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleId(user.getRoleId());
        vo.setHospitalId(user.getHospitalId());

        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role != null) {
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
        }

        return vo;
    }
}