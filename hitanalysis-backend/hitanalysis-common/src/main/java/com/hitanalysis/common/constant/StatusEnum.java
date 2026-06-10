package com.hitanalysis.common.constant;

import lombok.Getter;

/**
 * Status enumeration
 */
@Getter
public enum StatusEnum {

    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final int code;
    private final String desc;

    StatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum fromCode(int code) {
        for (StatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}