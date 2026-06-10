package com.hitanalysis.system.controller;

import com.hitanalysis.common.result.Result;
import com.hitanalysis.system.dto.LoginDTO;
import com.hitanalysis.system.service.AuthService;
import com.hitanalysis.system.vo.LoginVO;
import com.hitanalysis.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@Tag(name = "认证管理", description = "登录、登出、Token刷新等认证相关接口")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "通过用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO result = authService.login(dto);
        return Result.success(result);
    }

    @Operation(summary = "用户登出", description = "清除用户Token缓存")
    @PostMapping("/logout")
    public Result<Void> logout(@Parameter(description = "用户ID") @RequestParam Long userId) {
        authService.logout(userId);
        return Result.success();
    }

    @Operation(summary = "刷新Token", description = "使用refreshToken获取新的accessToken")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        LoginVO result = authService.refreshToken(refreshToken);
        return Result.success(result);
    }

    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息，包括权限和菜单")
    @GetMapping("/user-info")
    public Result<UserInfoVO> getUserInfo(@Parameter(description = "用户ID") @RequestParam Long userId) {
        UserInfoVO result = authService.getCurrentUser(userId);
        return Result.success(result);
    }
}