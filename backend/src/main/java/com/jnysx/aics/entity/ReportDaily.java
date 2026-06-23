package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据报表缓存表
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_report_daily")
public class ReportDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private String channel;
    private Integer totalSessions;
    private Integer botResolved;
    private Integer transferCount;
    private Integer avgResponseTime;
    private Integer avgBotResponseTime;
    private Integer avgAgentResponseTime;
    private BigDecimal csatAvg;
    /** 热门意图TOP10（JSON） */
    private String topIntents;
    /** 热门问题TOP10（JSON） */
    private String topQuestions;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public Integer getBotResolved() { return botResolved; }
    public void setBotResolved(Integer botResolved) { this.botResolved = botResolved; }

    public Integer getTransferCount() { return transferCount; }
    public void setTransferCount(Integer transferCount) { this.transferCount = transferCount; }

    public Integer getAvgResponseTime() { return avgResponseTime; }
    public void setAvgResponseTime(Integer avgResponseTime) { this.avgResponseTime = avgResponseTime; }

    public Integer getAvgBotResponseTime() { return avgBotResponseTime; }
    public void setAvgBotResponseTime(Integer avgBotResponseTime) { this.avgBotResponseTime = avgBotResponseTime; }

    public Integer getAvgAgentResponseTime() { return avgAgentResponseTime; }
    public void setAvgAgentResponseTime(Integer avgAgentResponseTime) { this.avgAgentResponseTime = avgAgentResponseTime; }

    public BigDecimal getCsatAvg() { return csatAvg; }
    public void setCsatAvg(BigDecimal csatAvg) { this.csatAvg = csatAvg; }

    public String getTopIntents() { return topIntents; }
    public void setTopIntents(String topIntents) { this.topIntents = topIntents; }

    public String getTopQuestions() { return topQuestions; }
    public void setTopQuestions(String topQuestions) { this.topQuestions = topQuestions; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

}