package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话上下文表（AI多轮对话记忆）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_dialog_context")
public class DialogContext {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    /** 槽位数据（JSON） */
    private String slotData;
    private String currentIntent;
    /** 对话状态：init/collecting/confirming/executing/completed */
    private String dialogState;
    /** 待填充槽位列表（JSON） */
    private String pendingSlots;
    /** 对话轮次摘要（JSON） */
    private String turnHistory;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSlotData() { return slotData; }
    public void setSlotData(String slotData) { this.slotData = slotData; }

    public String getCurrentIntent() { return currentIntent; }
    public void setCurrentIntent(String currentIntent) { this.currentIntent = currentIntent; }

    public String getDialogState() { return dialogState; }
    public void setDialogState(String dialogState) { this.dialogState = dialogState; }

    public String getPendingSlots() { return pendingSlots; }
    public void setPendingSlots(String pendingSlots) { this.pendingSlots = pendingSlots; }

    public String getTurnHistory() { return turnHistory; }
    public void setTurnHistory(String turnHistory) { this.turnHistory = turnHistory; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

}