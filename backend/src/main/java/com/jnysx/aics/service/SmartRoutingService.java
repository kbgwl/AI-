package com.jnysx.aics.service;

import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.entity.AgentSkillGroup;
import com.jnysx.aics.entity.SkillGroup;
import com.jnysx.aics.mapper.AgentMapper;
import com.jnysx.aics.mapper.AgentSkillGroupMapper;
import com.jnysx.aics.mapper.SkillGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能路由服务 - 技能组 + 负载均衡
 * @author 空白格·AI
 */
@Service
public class SmartRoutingService {
    
    @Autowired
    private SkillGroupMapper skillGroupMapper;
    
    @Autowired
    private AgentSkillGroupMapper agentSkillGroupMapper;
    
    @Autowired
    private AgentMapper agentMapper;
    
    /**
     * 根据意图代码智能分配坐席
     * @param intentCode 意图代码
     * @return 推荐的坐席 ID，如果没有可用坐席返回 null
     */
    public Long routeByIntent(String intentCode) {
        // 1. 查找匹配该意图的技能组
        List<SkillGroup> matchingGroups = findMatchingSkillGroups(intentCode);
        if (matchingGroups.isEmpty()) {
            return null;
        }
        
        // 2. 按优先级排序
        matchingGroups.sort(Comparator.comparingInt(SkillGroup::getPriority));
        SkillGroup targetGroup = matchingGroups.get(0);
        
        // 3. 查找该技能组下的可用坐席
        List<Agent> availableAgents = findAvailableAgentsBySkillGroup(targetGroup.getId());
        if (availableAgents.isEmpty()) {
            return null;
        }
        
        // 4. 负载均衡：选择当前会话数最少的坐席
        return selectAgentWithLeastLoad(availableAgents);
    }
    
    /**
     * 查找匹配意图的技能组
     */
    private List<SkillGroup> findMatchingSkillGroups(String intentCode) {
        // 查找所有启用的技能组
        List<SkillGroup> allGroups = skillGroupMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SkillGroup>()
                .eq(SkillGroup::getEnabled, true)
        );
        
        // 过滤出匹配该意图的技能组
        return allGroups.stream()
            .filter(group -> {
                if (group.getIntentCodes() == null || group.getIntentCodes().isEmpty()) {
                    return false;
                }
                String[] codes = group.getIntentCodes().split(",");
                for (String code : codes) {
                    if (code.trim().equals(intentCode)) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 查找技能组下的可用坐席
     */
    private List<Agent> findAvailableAgentsBySkillGroup(Long skillGroupId) {
        // 查找该技能组下的坐席
        List<AgentSkillGroup> agentSkills = agentSkillGroupMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkillGroup>()
                .eq(AgentSkillGroup::getSkillGroupId, skillGroupId)
                .eq(AgentSkillGroup::getEnabled, true)
        );
        
        if (agentSkills.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> agentIds = agentSkills.stream()
            .map(AgentSkillGroup::getAgentId)
            .collect(Collectors.toList());
        
        // 查找在线且有容量的坐席
        List<Agent> agents = agentMapper.selectBatchIds(agentIds);
        return agents.stream()
            .filter(agent -> agent.getStatus() == 1) // 在线状态
            .filter(agent -> {
                // 检查是否超过最大并发
                Integer currentLoad = agent.getCurrentSessions() != null ? agent.getCurrentSessions() : 0;
                Integer maxConcurrent = agent.getMaxSessions() != null ? agent.getMaxSessions() : 5;
                return currentLoad < maxConcurrent;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 选择负载最小的坐席
     */
    private Long selectAgentWithLeastLoad(List<Agent> agents) {
        return agents.stream()
            .min(Comparator.comparingInt(a -> 
                a.getCurrentSessions() != null ? a.getCurrentSessions() : 0
            ))
            .map(Agent::getId)
            .orElse(null);
    }
    
    /**
     * 获取所有技能组
     */
    public List<SkillGroup> getAllSkillGroups() {
        return skillGroupMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SkillGroup>()
                .orderByDesc(SkillGroup::getPriority)
        );
    }
    
    /**
     * 获取坐席的技能组列表
     */
    public List<SkillGroup> getAgentSkills(Long agentId) {
        List<AgentSkillGroup> agentSkills = agentSkillGroupMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkillGroup>()
                .eq(AgentSkillGroup::getAgentId, agentId)
                .eq(AgentSkillGroup::getEnabled, true)
        );
        
        if (agentSkills.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> skillGroupIds = agentSkills.stream()
            .map(AgentSkillGroup::getSkillGroupId)
            .collect(Collectors.toList());
        
        return skillGroupMapper.selectBatchIds(skillGroupIds);
    }
    
    /**
     * 为坐席分配技能组
     */
    public boolean assignSkillToAgent(Long agentId, Long skillGroupId, Integer proficiency) {
        // 检查是否已存在
        AgentSkillGroup existing = agentSkillGroupMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkillGroup>()
                .eq(AgentSkillGroup::getAgentId, agentId)
                .eq(AgentSkillGroup::getSkillGroupId, skillGroupId)
        );
        
        if (existing != null) {
            // 更新熟练度
            existing.setProficiency(proficiency);
            existing.setEnabled(true);
            return agentSkillGroupMapper.updateById(existing) > 0;
        } else {
            // 新增
            AgentSkillGroup agentSkill = new AgentSkillGroup();
            agentSkill.setAgentId(agentId);
            agentSkill.setSkillGroupId(skillGroupId);
            agentSkill.setProficiency(proficiency);
            agentSkill.setEnabled(true);
            return agentSkillGroupMapper.insert(agentSkill) > 0;
        }
    }
    
    /**
     * 移除坐席的技能组
     */
    public boolean removeSkillFromAgent(Long agentId, Long skillGroupId) {
        AgentSkillGroup agentSkill = agentSkillGroupMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkillGroup>()
                .eq(AgentSkillGroup::getAgentId, agentId)
                .eq(AgentSkillGroup::getSkillGroupId, skillGroupId)
        );
        
        if (agentSkill != null) {
            return agentSkillGroupMapper.deleteById(agentSkill) > 0;
        }
        return false;
    }
}