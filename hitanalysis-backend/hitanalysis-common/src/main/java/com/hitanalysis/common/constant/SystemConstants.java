package com.hitanalysis.common.constant;

/**
 * System constants
 */
public final class SystemConstants {

    public static final String DEFAULT_PASSWORD = "123456";

    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;

    public static final int PAGE_SIZE_DEFAULT = 10;
    public static final int PAGE_SIZE_MAX = 100;

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final long SESSION_TIMEOUT_MINUTES = 15;

    public static final String CACHE_PERMISSION_KEY = "user:permission:";
    public static final String CACHE_MENU_KEY = "user:menu:";
    public static final String CACHE_TOKEN_KEY = "user:token:";
}