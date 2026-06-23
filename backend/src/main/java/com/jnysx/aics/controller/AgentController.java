package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.entity.Session;
import com.jnysx.aics.service.AgentService;
import com.jnysx.aics.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class AgentController {

    private final AgentService agentService;
    private final SessionService sessionService;

    @GetMapping("/agents")
    public Result<?> listAgents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String skillGroup,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        if (skillGroup != null) wrapper.eq(Agent::getSkillGroup, skillGroup);
        if (status != null) wrapper.eq(Agent::getStatus, status);
        wrapper.orderByAsc(Agent::getStatus).orderByAsc(Agent::getCurrentSessions);
        Page<Agent> result = agentService.page(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @PostMapping("/agent")
    public Result<?> addAgent(@RequestBody Agent agent) {
        agent.setCreateTime(LocalDateTime.now());
        agent.setUpdateTime(LocalDateTime.now());
        agent.setCurrentSessions(0);
        agentService.save(agent);
        return Result.ok("客服创建成功", agent);
    }

    @PutMapping("/agent")
    public Result<?> updateAgent(@RequestBody Agent agent) {
        agent.setUpdateTime(LocalDateTime.now());
        agentService.updateById(agent);
        return Result.ok("客服更新成功");
    }

    @PutMapping("/agent/{id}/status")
    public Result<?> updateAgentStatus(@PathVariable Long id, @RequestParam Integer status) {
        Agent agent = agentService.getById(id);
        if (agent == null) return Result.fail("客服不存在");
        agent.setStatus(status);
        agent.setUpdateTime(LocalDateTime.now());
        agentService.updateById(agent);
        return Result.ok("状态更新成功");
    }

    @GetMapping("/agent/{agentId}/sessions")
    public Result<?> agentSessions(@PathVariable Long agentId) {
        List<Session> sessions = sessionService.list(
            new LambdaQueryWrapper<Session>()
                .eq(Session::getAgentId, agentId)
                .in(Session::getStatus, List.of(1, 2))
                .orderByAsc(Session::getStartedAt));
        return Result.ok(sessions);
    }
}
