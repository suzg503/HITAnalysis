package com.hitanalysis.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Indicator system entity (bi_indicator_system)
 */
@Data
@TableName("bi_indicator_system")
public class BiIndicatorSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "system_id", type = IdType.AUTO)
    private Long systemId;

    @TableField("system_code")
    private String systemCode;

    @TableField("system_name")
    private String systemName;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

    @TableField("sort_num")
    private Integer sortNum;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}