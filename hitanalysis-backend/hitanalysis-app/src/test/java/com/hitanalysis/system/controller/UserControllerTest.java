package com.hitanalysis.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitanalysis.common.result.Result;
import com.hitanalysis.system.dto.UserDTO;
import com.hitanalysis.system.vo.UserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController integration tests
 *
 * Tests user management REST API endpoints including:
 * - User creation
 * - User retrieval
 * - User update
 * - User deletion
 * - User listing
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController API Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private UserInfoVO testUserInfoVO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setPassword("testpassword");
        testUserDTO.setRealName("Test User");
        testUserDTO.setRoleId(1L);

        testUserInfoVO = new UserInfoVO();
        testUserInfoVO.setUserId(1L);
        testUserInfoVO.setUsername("testuser");
        testUserInfoVO.setRealName("Test User");
    }

    @Test
    @DisplayName("Create user endpoint should return success")
    void testCreateUser_Success() throws Exception {
        // Given
        when(userService.createUser(any(UserDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(userService, times(1)).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("Get user by ID endpoint should return user info")
    void testGetUserById_Success() throws Exception {
        // Given
        SysUser testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        when(userService.getById(anyLong())).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).getById(anyLong());
    }

    @Test
    @DisplayName("Update user endpoint should return success")
    void testUpdateUser_Success() throws Exception {
        // Given
        doNothing().when(userService).updateUser(any(UserDTO.class));

        testUserDTO.setUserId(1L);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("Delete user endpoint should return success")
    void testDeleteUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).deleteUser(anyLong());
    }

    @Test
    @DisplayName("List users endpoint should return page result")
    void testListUsers_Success() throws Exception {
        // Given
        PageResult<UserInfoVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(testUserInfoVO));
        pageResult.setTotal(1);

        when(userService.listUsers(anyInt(), anyInt(), any(), any())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"));

        verify(userService, times(1)).listUsers(anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Change user status endpoint should return success")
    void testChangeStatus_Success() throws Exception {
        // Given
        doNothing().when(userService).changeStatus(anyLong(), anyInt());

        // When & Then
        mockMvc.perform(put("/api/v1/users/1/status")
                .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).changeStatus(anyLong(), anyInt());
    }
}