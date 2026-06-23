package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.Agent;

import java.util.Optional;

/**
 * 客服人员Service接口
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
public interface AgentService extends IService<Agent> {
    /**
     * 查找可用的客服（在线且未满负荷）
     */
    Optional<Agent> findAvailableAgent();
    
    /**
     * 查找指定技能组的可用客服
     */
    Optional<Agent> findAvailableAgentBySkillGroup(String skillGroup);
    
    /**
     * 增加客服当前会话数
     */
    boolean incrementCurrentSessions(Long agentId);
    
    /**
     * 减少客服当前会话数
     */
    boolean decrementCurrentSessions(Long agentId);
    
    /**
     * 根据用户名获取客服
     */
    Agent getByUsername(String username);
}