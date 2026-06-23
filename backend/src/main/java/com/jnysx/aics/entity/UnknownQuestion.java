package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 未知问题表（自学习）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_unknown_question")
public class UnknownQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String question;
    private String sessionId;
    private Long similarKnownId;
    /** 状态：0-待处理，1-已补充答案，2-已忽略，3-已归并 */
    private Integer status;
    private String suggestedAnswer;
    private String handleBy;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getSimilarKnownId() { return similarKnownId; }
    public void setSimilarKnownId(Long similarKnownId) { this.similarKnownId = similarKnownId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getSuggestedAnswer() { return suggestedAnswer; }
    public void setSuggestedAnswer(String suggestedAnswer) { this.suggestedAnswer = suggestedAnswer; }

    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }

    public LocalDateTime getHandleTime() { return handleTime; }
    public void setHandleTime(LocalDateTime handleTime) { this.handleTime = handleTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

}