package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 坐席技能组实体
 * @author 空白格·AI
 */
@TableName("tb_agent_skill_group")
@Data
public class AgentSkillGroup {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 坐席 ID */
    @TableField("agent_id")
    private Long agentId;
    
    /** 技能组 ID */
    @TableField("skill_group_id")
    private Long skillGroupId;
    
    /** 熟练度：1-初级，2-中级，3-高级，4-专家 */
    @TableField("proficiency")
    private Integer proficiency;
    
    /** 是否启用该技能 */
    @TableField("enabled")
    private Boolean enabled;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}