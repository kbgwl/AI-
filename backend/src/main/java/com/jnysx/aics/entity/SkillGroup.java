package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 技能组实体
 * @author 空白格·AI
 */
@TableName("tb_skill_group")
@Data
public class SkillGroup {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 技能组名称 */
    @TableField("name")
    private String name;
    
    /** 技能组编码 */
    @TableField("code")
    private String code;
    
    /** 描述 */
    @TableField("description")
    private String description;
    
    /** 优先级：数字越小优先级越高 */
    @TableField("priority")
    private Integer priority;
    
    /** 最大并发会话数 */
    @TableField("max_concurrent")
    private Integer maxConcurrent;
    
    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;
    
    /** 关联意图代码（逗号分隔） */
    @TableField("intent_codes")
    private String intentCodes;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}