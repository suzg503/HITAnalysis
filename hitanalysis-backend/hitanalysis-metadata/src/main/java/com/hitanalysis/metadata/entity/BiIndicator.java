package com.hitanalysis.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Indicator entity (bi_indicator)
 */
@Data
@TableName("bi_indicator")
public class BiIndicator implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "zb_id", type = IdType.AUTO)
    private Long zbId;

    @TableField("parent_zb_id")
    private Long parentZbId;

    @TableField("zb_code")
    private String zbCode;

    @TableField("zb_name")
    private String zbName;

    @TableField("system_id")
    private Long systemId;

    @TableField("cat_id")
    private Long catId;

    @TableField("zb_meaning")
    private String zbMeaning;

    @TableField("zb_caliber")
    private String zbCaliber;

    @TableField("is_real_time")
    private Integer isRealTime;

    @TableField("has_decimal")
    private Integer hasDecimal;

    @TableField("ratio_type")
    private String ratioType;

    @TableField("unit")
    private String unit;

    @TableField("config_type")
    private Integer configType;

    @TableField("status")
    private Integer status;

    @TableField("version")
    private Integer version;

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