package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工单表
 * @author 空白格·AI
 */
@TableName("tb_ticket")
@Data
public class Ticket {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 工单号（唯一） */
    private String ticketNo;
    
    /** 会话 ID */
    private String sessionId;
    
    /** 用户 ID */
    private String userId;
    
    /** 工单类型 */
    private String ticketType; // complaint, complex, followup, escalation
    
    /** 优先级：1-低，2-中，3-高，4-紧急 */
    private Integer priority;
    
    /** 标题 */
    private String title;
    
    /** 问题描述 */
    private String description;
    
    /** 关联意图 */
    private String intentCode;
    
    /** 用户情绪分（-2 到 2） */
    private Integer sentimentScore;
    
    /** 处理人（坐席 ID） */
    private String assigneeId;
    
    /** 处理人姓名 */
    private String assigneeName;
    
    /** 状态：pending, processing, resolved, closed */
    private String status;
    
    /** 解决方案 */
    private String solution;
    
    /** 客户满意度（1-5 星） */
    private Integer satisfaction;
    
    /** 创建人 */
    private String creatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
