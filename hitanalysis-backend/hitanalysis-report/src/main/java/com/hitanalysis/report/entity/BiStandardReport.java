package com.hitanalysis.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Standard report entity (bi_standard_report)
 */
@Data
@TableName("bi_standard_report")
public class BiStandardReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("report_code")
    private String reportCode;

    @TableField("report_name")
    private String reportName;

    @TableField("report_level")
    private Integer reportLevel;

    @TableField("report_url")
    private String reportUrl;

    @TableField("zb_ids")
    private String zbIds;

    @TableField("zb_names")
    private String zbNames;

    @TableField("system_id")
    private Long systemId;

    @TableField("cat_id")
    private Long catId;

    @TableField("sort_num")
    private Integer sortNum;

    @TableField("remark")
    private String remark;

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