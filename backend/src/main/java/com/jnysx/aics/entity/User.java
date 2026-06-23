package com.jnysx.aics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表（C端用户/访客）
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@TableName("tb_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户类型：0-访客，1-注册用户，2-VIP用户 */
    private Integer userType;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL */
    private String avatar;
    /** 手机号（加密存储） */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 密码哈希 */
    private String passwordHash;
    /** 偏好语言 */
    private String language;
    /** VIP等级：0-普通，1-银卡，2-金卡，3-钻石 */
    private Integer vipLevel;
    /** 用户标签（JSON数组） */
    private String tags;
    /** 最后活跃时间 */
    private LocalDateTime lastActiveTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getVipLevel() { return vipLevel; }
    public void setVipLevel(Integer vipLevel) { this.vipLevel = vipLevel; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

}