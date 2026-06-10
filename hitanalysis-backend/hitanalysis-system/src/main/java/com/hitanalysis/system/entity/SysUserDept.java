package com.hitanalysis.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User department relation entity (sys_user_dept)
 */
@Data
@TableName("sys_user_dept")
public class SysUserDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("dept_code")
    private String deptCode;

    @TableField("dept_name")
    private String deptName;

    @TableField("dept_type")
    private Integer deptType;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}