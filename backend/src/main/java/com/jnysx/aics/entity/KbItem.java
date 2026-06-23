package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库条目表（FAQ+文档）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
 @TableName("tb_kb_item")
public class KbItem {
 @TableId(type = IdType.AUTO)
 private Long id;
 private Long categoryId;
 private Long industryId; // 行业 ID
 /** 类型：faq/document/table/api */
 private String itemType;
    private String question;
    private String answer;
    /** 相似问法列表（JSON） */
    private String similarQuestions;
    private String keywords;
    private String tags;
    private Integer priority;
    private Integer hitCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private LocalDateTime effectiveStart;
    private LocalDateTime effectiveEnd;
    private Integer version;
    /** 状态：0-禁用，1-启用，2-待审核 */
    private Integer status;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getIndustryId() { return industryId; }
    public void setIndustryId(Long industryId) { this.industryId = industryId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getSimilarQuestions() { return similarQuestions; }
    public void setSimilarQuestions(String similarQuestions) { this.similarQuestions = similarQuestions; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }

    public LocalDateTime getEffectiveStart() { return effectiveStart; }
    public void setEffectiveStart(LocalDateTime effectiveStart) { this.effectiveStart = effectiveStart; }

    public LocalDateTime getEffectiveEnd() { return effectiveEnd; }
    public void setEffectiveEnd(LocalDateTime effectiveEnd) { this.effectiveEnd = effectiveEnd; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}