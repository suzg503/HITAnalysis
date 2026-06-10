package com.hitanalysis.common.constant;

import lombok.Getter;

/**
 * Data permission type enumeration (D3 decision implementation)
 *
 * Controls what data scope a user can access
 */
@Getter
public enum DataPermissionType {

    SELF(1, "仅本人", "creator_id = {user_id}"),
    DEPT(2, "仅科室", "dept_id IN (SELECT dept_id FROM sys_user_dept WHERE user_id = {user_id})"),
    HOSPITAL(3, "仅院区", "hospital_id IN (SELECT hospital_id FROM sys_user_hospital WHERE user_id = {user_id})"),
    CROSS_HOSPITAL(4, "跨院区", "hospital_id IN ({authorized_hospital_ids})"),
    ALL(5, "全部数据", "");

    private final int code;
    private final String desc;
    private final String sqlTemplate;

    DataPermissionType(int code, String desc, String sqlTemplate) {
        this.code = code;
        this.desc = desc;
        this.sqlTemplate = sqlTemplate;
    }

    public static DataPermissionType fromCode(int code) {
        for (DataPermissionType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return SELF;
    }
}