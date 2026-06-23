package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.mapper.AgentMapper;
import com.jnysx.aics.service.AgentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 客服人员Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    
    @Override
    public Optional<Agent> findAvailableAgent() {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getStatus, 1) // 在线
               .eq(Agent::getDeleted, 0)
               .apply("current_sessions < max_sessions") // 未满负荷
               .orderByAsc(Agent::getCurrentSessions) // 优先分配给会话少的客服
               .last("LIMIT 1");
        return Optional.ofNullable(this.getOne(wrapper));
    }
    
    @Override
    public Optional<Agent> findAvailableAgentBySkillGroup(String skillGroup) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getStatus, 1) // 在线
               .eq(Agent::getDeleted, 0)
               .eq(Agent::getSkillGroup, skillGroup)
               .apply("current_sessions < max_sessions") // 未满负荷
               .orderByAsc(Agent::getCurrentSessions)
               .last("LIMIT 1");
        return Optional.ofNullable(this.getOne(wrapper));
    }
    
    @Override
    @Transactional
    public boolean incrementCurrentSessions(Long agentId) {
        Agent agent = this.getById(agentId);
        if (agent == null) {
            return false;
        }
        agent.setCurrentSessions(agent.getCurrentSessions() + 1);
        // 如果客服会话数达到最大值，自动设为忙碌
        if (agent.getCurrentSessions() >= agent.getMaxSessions()) {
            agent.setStatus(2); // 忙碌
        }
        return this.updateById(agent);
    }
    
    @Override
    @Transactional
    public boolean decrementCurrentSessions(Long agentId) {
        Agent agent = this.getById(agentId);
        if (agent == null) {
            return false;
        }
        agent.setCurrentSessions(Math.max(0, agent.getCurrentSessions() - 1));
        // 如果客服之前是忙碌状态，现在可以恢复在线
        if (agent.getStatus() == 2 && agent.getCurrentSessions() < agent.getMaxSessions()) {
            agent.setStatus(1); // 在线
        }
        return this.updateById(agent);
    }
    
    @Override
    public Agent getByUsername(String username) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getUsername, username)
               .eq(Agent::getDeleted, 0);
        return this.getOne(wrapper);
    }
}