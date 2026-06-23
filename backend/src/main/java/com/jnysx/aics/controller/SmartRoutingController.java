package com.jnysx.aics.controller;

import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.AgentSkillGroup;
import com.jnysx.aics.entity.SkillGroup;
import com.jnysx.aics.service.SmartRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能路由管理 Controller
 * @author 空白格·AI
 */
@RestController
@RequestMapping("/api/routing")
@CrossOrigin(origins = {"https://ai-cs.jnysx.cn"}, allowedHeaders = "*", allowCredentials = "true")
@RequireLogin
public class SmartRoutingController {
    
    @Autowired
    private SmartRoutingService smartRoutingService;
    
    /**
     * 获取所有技能组
     */
    @GetMapping("/skill-groups")
    public Result<List<SkillGroup>> getSkillGroups() {
        try {
            List<SkillGroup> groups = smartRoutingService.getAllSkillGroups();
            return Result.ok(groups);
        } catch (Exception e) {
            return Result.fail("获取技能组失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取坐席的技能组
     */
    @GetMapping("/agent/{agentId}/skills")
    public Result<List<SkillGroup>> getAgentSkills(@PathVariable Long agentId) {
        try {
            List<SkillGroup> skills = smartRoutingService.getAgentSkills(agentId);
            return Result.ok(skills);
        } catch (Exception e) {
            return Result.fail("获取坐席技能失败：" + e.getMessage());
        }
    }
    
    /**
     * 为坐席分配技能组
     */
    @PostMapping("/agent/{agentId}/skill/{skillGroupId}")
    public Result<String> assignSkill(
            @PathVariable Long agentId,
            @PathVariable Long skillGroupId,
            @RequestParam(defaultValue = "2") Integer proficiency) {
        try {
            boolean success = smartRoutingService.assignSkillToAgent(agentId, skillGroupId, proficiency);
            return success ? Result.ok("技能分配成功") : Result.fail("技能分配失败");
        } catch (Exception e) {
            return Result.fail("技能分配失败：" + e.getMessage());
        }
    }
    
    /**
     * 移除坐席的技能组
     */
    @DeleteMapping("/agent/{agentId}/skill/{skillGroupId}")
    public Result<String> removeSkill(
            @PathVariable Long agentId,
            @PathVariable Long skillGroupId) {
        try {
            boolean success = smartRoutingService.removeSkillFromAgent(agentId, skillGroupId);
            return success ? Result.ok("技能移除成功") : Result.fail("技能移除失败");
        } catch (Exception e) {
            return Result.fail("技能移除失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试路由 - 根据意图推荐坐席
     */
    @GetMapping("/test-route")
    public Result<Map<String, Object>> testRoute(@RequestParam String intentCode) {
        try {
            Long recommendedAgentId = smartRoutingService.routeByIntent(intentCode);
            
            Map<String, Object> result = new HashMap<>();
            result.put("intentCode", intentCode);
            result.put("recommendedAgentId", recommendedAgentId);
            result.put("message", recommendedAgentId != null ? 
                "推荐坐席 ID: " + recommendedAgentId : "暂无可用坐席");
            
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("路由测试失败：" + e.getMessage());
        }
    }
}