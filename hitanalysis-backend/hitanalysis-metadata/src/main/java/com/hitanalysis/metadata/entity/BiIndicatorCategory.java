package com.hitanalysis.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Indicator category entity (bi_indicator_category)
 */
@Data
@TableName("bi_indicator_category")
public class BiIndicatorCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "cat_id", type = IdType.AUTO)
    private Long catId;

    @TableField("system_id")
    private Long systemId;

    @TableField("cat_code")
    private String catCode;

    @TableField("cat_name")
    private String catName;

    @TableField("sort_num")
    private Integer sortNum;

    @TableField("status")
    private Integer status;

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