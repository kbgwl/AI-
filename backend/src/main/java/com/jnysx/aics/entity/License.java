package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 软件授权许可证表
 */
@TableName("tb_license")
public class License {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String licenseKey;
    private String machineCode;
    private String customerName;
    private String customerContact;
    /** 套餐类型：trial/basic/pro/enterprise */
    private String planType;
    /** 最大坐席数 */
    private Integer maxAgents;
    /** 最大会话数 */
    private Integer maxSessions;
    /** 许可类型：time-lifetime */
    private String licenseType;
    /** 到期时间（time类型） */
    private LocalDateTime expireTime;
    /** 激活时间 */
    private LocalDateTime activateTime;
    /** 状态：0-未激活 1-已激活 2-已过期 3-已禁用 */
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerContact() { return customerContact; }
    public void setCustomerContact(String customerContact) { this.customerContact = customerContact; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public Integer getMaxAgents() { return maxAgents; }
    public void setMaxAgents(Integer maxAgents) { this.maxAgents = maxAgents; }
    public Integer getMaxSessions() { return maxSessions; }
    public void setMaxSessions(Integer maxSessions) { this.maxSessions = maxSessions; }
    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public LocalDateTime getActivateTime() { return activateTime; }
    public void setActivateTime(LocalDateTime activateTime) { this.activateTime = activateTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
