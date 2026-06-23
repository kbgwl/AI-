package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 客服人员表（人工坐席）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_agent")
public class Agent {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    /** 技能组 */
    private String skillGroup;
    /** 状态：0-离线，1-在线，2-忙碌，3-离开 */
    private Integer status;
    /** 最大同时服务会话数 */
    private Integer maxSessions;
    /** 当前服务会话数 */
    private Integer currentSessions;
    /** 角色：agent/supervisor/admin */
    private String role;
    /** 租户ID */
    private Long tenantId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSkillGroup() { return skillGroup; }
    public void setSkillGroup(String skillGroup) { this.skillGroup = skillGroup; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getMaxSessions() { return maxSessions; }
    public void setMaxSessions(Integer maxSessions) { this.maxSessions = maxSessions; }

    public Integer getCurrentSessions() { return currentSessions; }
    public void setCurrentSessions(Integer currentSessions) { this.currentSessions = currentSessions; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}