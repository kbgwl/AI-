package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 外呼模板实体
 * @author 空白格·AI
 */
@TableName("tb_outreach_template")
@Data
public class OutreachTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 模板名称 */
    @TableField("name")
    private String name;
    
    /** 模板类型：sms-短信，email-邮件，notification-站内通知 */
    @TableField("task_type")
    private String taskType;
    
    /** 模板内容 */
    @TableField("content")
    private String content;
    
    /** 变量说明（JSON） */
    @TableField("variables")
    private String variables;
    
    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;
    
    /** 使用次数 */
    @TableField("usage_count")
    private Integer usageCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}