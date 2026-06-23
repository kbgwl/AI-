package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 渠道配置表
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_channel_config")
public class ChannelConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String channel;
    private String configName;
    /** 配置数据（JSON） */
    private String configData;
    private String welcomeMessage;
    /** 常见问题菜单（JSON） */
    private String faqMenu;
    private Integer botEnabled;
    private Integer transferEnabled;
    private String workTimeStart;
    private String workTimeEnd;
    private String workDays;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public String getConfigData() { return configData; }
    public void setConfigData(String configData) { this.configData = configData; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }

    public String getFaqMenu() { return faqMenu; }
    public void setFaqMenu(String faqMenu) { this.faqMenu = faqMenu; }

    public Integer getBotEnabled() { return botEnabled; }
    public void setBotEnabled(Integer botEnabled) { this.botEnabled = botEnabled; }

    public Integer getTransferEnabled() { return transferEnabled; }
    public void setTransferEnabled(Integer transferEnabled) { this.transferEnabled = transferEnabled; }

    public String getWorkTimeStart() { return workTimeStart; }
    public void setWorkTimeStart(String workTimeStart) { this.workTimeStart = workTimeStart; }

    public String getWorkTimeEnd() { return workTimeEnd; }
    public void setWorkTimeEnd(String workTimeEnd) { this.workTimeEnd = workTimeEnd; }

    public String getWorkDays() { return workDays; }
    public void setWorkDays(String workDays) { this.workDays = workDays; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}