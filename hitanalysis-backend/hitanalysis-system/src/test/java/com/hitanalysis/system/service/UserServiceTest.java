package com.hitanalysis.system.service;

import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.system.dto.UserDTO;
import com.hitanalysis.system.entity.SysUser;
import com.hitanalysis.system.vo.UserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService unit tests
 *
 * Tests user management operations including:
 * - User creation
 * - User update
 * - User deletion
 * - Password management
 * - Status management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO testUserDTO;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setPassword("testpassword");
        testUserDTO.setRealName("Test User");
        testUserDTO.setRoleId(1L);
        testUserDTO.setHospitalId(1L);

        testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("Test User");
        testUser.setRoleId(1L);
        testUser.setHospitalId(1L);
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("Create user successfully")
    void testCreateUser_Success() {
        // Given
        when(passwordUtils.encode(anyString())).thenReturn("hashed_password");
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);

        // When
        Long userId = userService.createUser(testUserDTO);

        // Then
        assertNotNull(userId);
        verify(userMapper, times(1)).insert(any(SysUser.class));
        verify(passwordUtils, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Create user with duplicate username fails")
    void testCreateUser_DuplicateUsername() {
        // Given
        when(userMapper.selectByUsernameWithRole(anyString())).thenReturn(testUser);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.createUser(testUserDTO);
        });

        verify(userMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("Update user successfully")
    void testUpdateUser_Success() {
        // Given
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        testUserDTO.setUserId(1L);
        testUserDTO.setRealName("Updated Name");

        // When
        userService.updateUser(testUserDTO);

        // Then
        verify(userMapper, times(1)).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("Update non-existent user fails")
    void testUpdateUser_UserNotFound() {
        // Given
        when(userMapper.selectById(anyLong())).thenReturn(null);

        testUserDTO.setUserId(999L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.updateUser(testUserDTO);
        });

        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("Soft delete user successfully")
    void testDeleteUser_Success() {
        // Given
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userMapper.softDeleteById(anyLong())).thenReturn(1);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userMapper, times(1)).softDeleteById(anyLong());
    }

    @Test
    @DisplayName("List users with pagination")
    void testListUsers_Success() {
        // Given
        when(userMapper.selectUserList(any())).thenReturn(List.of(testUser));

        // When
        PageResult<UserInfoVO> result = userService.listUsers(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(userMapper, times(1)).selectUserList(any());
    }

    @Test
    @DisplayName("Change user status successfully")
    void testChangeStatus_Success() {
        // Given
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userMapper.updateStatus(anyLong(), anyInt())).thenReturn(1);

        // When
        userService.changeStatus(1L, 0);

        // Then
        verify(userMapper, times(1)).updateStatus(anyLong(), anyInt());
    }
}