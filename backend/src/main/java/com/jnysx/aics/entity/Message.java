package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息表
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    /** 消息类型：text/image/card/video/file/link/quick_reply/form/system */
    private String msgType;
    /** 发送者类型：user/agent/bot/system */
    private String senderType;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String mediaUrl;
    private String mediaType;
    /** 卡片数据（JSON） */
    private String cardData;
    /** 快捷回复按钮（JSON） */
    private String quickReplies;
    /** 表单数据（JSON） */
    private String formData;
    private String intent;
    /** 抽取到的实体（JSON） */
    private String entities;
    /** 情感：0-中性，1-正面，-1-负面 */
    private Integer sentiment;
    /** 是否已读 */
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getCardData() { return cardData; }
    public void setCardData(String cardData) { this.cardData = cardData; }

    public String getQuickReplies() { return quickReplies; }
    public void setQuickReplies(String quickReplies) { this.quickReplies = quickReplies; }

    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getEntities() { return entities; }
    public void setEntities(String entities) { this.entities = entities; }

    public Integer getSentiment() { return sentiment; }
    public void setSentiment(Integer sentiment) { this.sentiment = sentiment; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

}