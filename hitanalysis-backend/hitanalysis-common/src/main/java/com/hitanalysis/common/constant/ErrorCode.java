package com.hitanalysis.common.constant;

import lombok.Getter;

/**
 * Error codes enumeration
 */
@Getter
public enum ErrorCode {

    // Common errors (1xxx)
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(1000, "系统异常"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_INVALID(1003, "参数格式不正确"),

    // Authentication errors (2xxx)
    TOKEN_INVALID(2000, "Token无效"),
    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_MISSING(2002, "缺少Token"),
    LOGIN_FAILED(2003, "登录失败"),
    LOGIN_PASSWORD_ERROR(2004, "密码错误"),
    LOGIN_USER_NOT_FOUND(2005, "用户不存在"),
    LOGIN_USER_DISABLED(2006, "用户已被禁用"),
    LOGIN_LOCKED(2007, "账户已被锁定"),
    LOGOUT_FAILED(2008, "登出失败"),

    // Permission errors (3xxx)
    PERMISSION_DENIED(3000, "权限不足"),
    PERMISSION_MENU_DENIED(3001, "无菜单访问权限"),
    PERMISSION_BUTTON_DENIED(3002, "无操作权限"),
    PERMISSION_DATA_DENIED(3003, "无数据访问权限"),
    PERMISSION_CROSS_HOSPITAL_DENIED(3004, "无跨院区查询权限"),  // D3
    PERMISSION_AI_DENIED(3005, "无AI功能使用权限"),

    // User errors (4xxx)
    USER_NOT_FOUND(4000, "用户不存在"),
    USER_ALREADY_EXISTS(4001, "用户已存在"),
    USER_PASSWORD_ERROR(4002, "密码错误"),
    USER_PASSWORD_OLD_ERROR(4003, "原密码错误"),
    USER_PASSWORD_NEW_SAME(4004, "新密码与原密码相同"),

    // Role errors (5xxx)
    ROLE_NOT_FOUND(5000, "角色不存在"),
    ROLE_ALREADY_EXISTS(5001, "角色已存在"),
    ROLE_NAME_DUPLICATE(5002, "角色名称重复"),
    ROLE_CODE_DUPLICATE(5003, "角色编码重复"),

    // Menu errors (6xxx)
    MENU_NOT_FOUND(6000, "菜单不存在"),
    MENU_ALREADY_EXISTS(6001, "菜单已存在"),
    MENU_HAS_CHILDREN(6002, "菜单存在子菜单，无法删除"),

    // Report errors (7xxx)
    REPORT_NOT_FOUND(7000, "报表不存在"),
    REPORT_CONFIG_ERROR(7001, "报表配置错误"),
    REPORT_QUERY_ERROR(7002, "报表查询失败"),
    REPORT_EXPORT_ERROR(7003, "报表导出失败"),

    // AI errors (8xxx)
    AI_PARSE_ERROR(8000, "AI语义解析失败"),
    AI_CONFIG_ERROR(8001, "AI配置生成失败"),
    AI_QUERY_ERROR(8002, "AI查询执行失败"),
    AI_INSIGHT_ERROR(8003, "AI洞察生成失败"),
    AI_SESSION_NOT_FOUND(8004, "AI会话不存在"),
    AI_QUERY_LIMIT_EXCEEDED(8005, "AI查询次数已用尽"),
    AI_INDICATOR_NOT_MATCHED(8006, "无法匹配指标"),
    AI_DIMENSION_NOT_MATCHED(8007, "无法匹配维度");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}