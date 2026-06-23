package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话流程表（多轮任务型对话）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_dialog_flow")
public class DialogFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String flowCode;
    private String flowName;
    private String intentCode;
    /** 流程配置（JSON：节点+边+条件） */
    private String flowConfig;
    /** 必需槽位（JSON） */
    private String requiredSlots;
    /** 外部API调用配置（JSON） */
    private String apiConfig;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }

    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

    public String getIntentCode() { return intentCode; }
    public void setIntentCode(String intentCode) { this.intentCode = intentCode; }

    public String getFlowConfig() { return flowConfig; }
    public void setFlowConfig(String flowConfig) { this.flowConfig = flowConfig; }

    public String getRequiredSlots() { return requiredSlots; }
    public void setRequiredSlots(String requiredSlots) { this.requiredSlots = requiredSlots; }

    public String getApiConfig() { return apiConfig; }
    public void setApiConfig(String apiConfig) { this.apiConfig = apiConfig; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}