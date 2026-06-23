package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话表
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_session")
public class Session {
 @TableId(type = IdType.AUTO)
 private Long id;
 private String sessionId;
 private Long userId;
 private Long agentId;
 private Long industryId; // 行业 ID
 /** 渠道：web/wechat/app/dingtalk/feishu/whatsapp */
 private String channel;
    private String channelDetail;
    /** 状态：0-机器人对话中，1-等待人工，2-人工服务中，3-已结束，4-用户离开 */
    private Integer status;
    /** 对话模式：0-AI自动，1-AI+人工辅助 */
    private Integer mode;
    private String userIp;
    private String userAgent;
    private String pageUrl;
    private String userLanguage;
    /** 用户情绪：0-中性，1-正面，-1-负面，-2-愤怒 */
    private Integer userSentiment;
    private Integer turnCount;
    private Integer queuePosition;
    private LocalDateTime startedAt;
    private LocalDateTime agentJoinedAt;
    private LocalDateTime endedAt;
    private String endReason;
    private Integer csatScore;
    private String csatComment;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public Long getIndustryId() { return industryId; }
    public void setIndustryId(Long industryId) { this.industryId = industryId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getChannelDetail() { return channelDetail; }
    public void setChannelDetail(String channelDetail) { this.channelDetail = channelDetail; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getMode() { return mode; }
    public void setMode(Integer mode) { this.mode = mode; }

    public String getUserIp() { return userIp; }
    public void setUserIp(String userIp) { this.userIp = userIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getUserLanguage() { return userLanguage; }
    public void setUserLanguage(String userLanguage) { this.userLanguage = userLanguage; }

    public Integer getUserSentiment() { return userSentiment; }
    public void setUserSentiment(Integer userSentiment) { this.userSentiment = userSentiment; }

    public Integer getTurnCount() { return turnCount; }
    public void setTurnCount(Integer turnCount) { this.turnCount = turnCount; }

    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getAgentJoinedAt() { return agentJoinedAt; }
    public void setAgentJoinedAt(LocalDateTime agentJoinedAt) { this.agentJoinedAt = agentJoinedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = endReason; }

    public Integer getCsatScore() { return csatScore; }
    public void setCsatScore(Integer csatScore) { this.csatScore = csatScore; }

    public String getCsatComment() { return csatComment; }
    public void setCsatComment(String csatComment) { this.csatComment = csatComment; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}