package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 意图定义表
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_intent")
public class Intent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String intentCode;
    private String intentName;
    private String category;
    /** 训练样本（JSON） */
    private String samples;
    private String responseTemplate;
    /** 是否需转人工 */
    private Integer needTransfer;
    private Integer priority;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIntentCode() { return intentCode; }
    public void setIntentCode(String intentCode) { this.intentCode = intentCode; }

    public String getIntentName() { return intentName; }
    public void setIntentName(String intentName) { this.intentName = intentName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSamples() { return samples; }
    public void setSamples(String samples) { this.samples = samples; }

    public String getResponseTemplate() { return responseTemplate; }
    public void setResponseTemplate(String responseTemplate) { this.responseTemplate = responseTemplate; }

    public Integer getNeedTransfer() { return needTransfer; }
    public void setNeedTransfer(Integer needTransfer) { this.needTransfer = needTransfer; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}