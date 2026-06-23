package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 外呼任务实体
 * @author 空白格·AI
 */
@TableName("tb_outreach_task")
@Data
public class OutreachTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 任务名称 */
    @TableField("name")
    private String name;
    
    /** 任务类型：sms-短信，email-邮件，notification-站内通知 */
    @TableField("task_type")
    private String taskType;
    
    /** 目标用户群：all-全部，new-新用户，inactive-沉睡用户 */
    @TableField("target_group")
    private String targetGroup;
    
    /** 模板 ID */
    @TableField("template_id")
    private Long templateId;
    
    /** 任务状态：pending-待执行，running-执行中，completed-已完成，cancelled-已取消 */
    @TableField("status")
    private String status;
    
    /** 总数量 */
    @TableField("total_count")
    private Integer totalCount;
    
    /** 已发送数量 */
    @TableField("sent_count")
    private Integer sentCount;
    
    /** 成功数量 */
    @TableField("success_count")
    private Integer successCount;
    
    /** 失败数量 */
    @TableField("failed_count")
    private Integer failedCount;
    
    /** 计划执行时间 */
    @TableField("scheduled_time")
    private LocalDateTime scheduledTime;
    
    /** 实际开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /** 实际结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /** 创建人 */
    @TableField("creator_id")
    private Long creatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}