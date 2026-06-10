package com.hitanalysis.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Indicator dimension configuration entity (bi_indicator_dimension)
 */
@Data
@TableName("bi_indicator_dimension")
public class BiIndicatorDimension implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "dim_id", type = IdType.AUTO)
    private Long dimId;

    @TableField("zb_id")
    private Long zbId;

    @TableField("fact_table")
    private String factTable;

    @TableField("measure_field")
    private String measureField;

    @TableField("dimension_field")
    private String dimensionField;

    @TableField("filter_condition")
    private String filterCondition;

    @TableField("aggregation_type")
    private String aggregationType;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}