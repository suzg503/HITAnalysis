package com.hitanalysis.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI report entity (bi_ai_report)
 */
@Data
@TableName("bi_ai_report")
public class BiAiReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("report_name")
    private String reportName;

    @TableField("config_json")
    private String configJson;

    @TableField("visibility")
    private String visibility;

    @TableField("folder_id")
    private Long folderId;

    @TableField("zb_ids")
    private String zbIds;

    @TableField("status")
    private String status;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}