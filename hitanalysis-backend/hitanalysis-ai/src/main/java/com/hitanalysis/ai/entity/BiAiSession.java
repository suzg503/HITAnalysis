package com.hitanalysis.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI session entity (bi_ai_session)
 */
@Data
@TableName("bi_ai_session")
public class BiAiSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("session_name")
    private String sessionName;

    @TableField("status")
    private String status;

    @TableField("message_count")
    private Integer messageCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("active_time")
    private LocalDateTime activeTime;
}