package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.service.AgentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AgentService agentService;

    @GetMapping("/captcha/{captchaId}")
    @RequireLogin(false)
    public ResponseEntity<String> getCaptcha(@PathVariable String captchaId, HttpSession session) {
        String code = String.format("%04d", (int)(Math.random() * 10000));
        session.setAttribute("captcha:" + captchaId, code);
        session.setMaxInactiveInterval(300);
        return ResponseEntity.ok(code);
    }

    @PostMapping("/login")
    @RequireLogin(false)
    public Result<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String password = request.get("password");
        String captchaInput = request.get("captcha");
        String captchaId = request.get("captchaId");

        if (captchaInput != null && captchaId != null) {
            String sessionCaptcha = (String) session.getAttribute("captcha:" + captchaId);
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captchaInput.trim())) {
                return Result.badRequest("验证码错误");
            }
            session.removeAttribute("captcha:" + captchaId);
        }

        if (username == null || password == null) {
            return Result.badRequest("用户名和密码不能为空");
        }

        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getUsername, username);
        Agent agent = agentService.getOne(wrapper);

        if (agent == null) {
            return Result.unauthorized("用户名或密码错误");
        }

        if (!BCrypt.checkpw(password, agent.getPasswordHash())) {
            return Result.unauthorized("用户名或密码错误");
        }

        if (agent.getStatus() != 1) {
            return Result.badRequest("账号已被禁用");
        }

        session.setAttribute("userId", agent.getId());
        session.setAttribute("user", agent);
        session.setAttribute("role", agent.getRole());
        session.setMaxInactiveInterval(1800);

        log.info("管理员登录成功：{} (role={}, skillGroup={})", username, agent.getRole(), agent.getSkillGroup());

        Map<String, Object> data = new HashMap<>();
        data.put("id", agent.getId());
        data.put("username", agent.getUsername());
        data.put("nickname", agent.getNickname());
        data.put("role", agent.getRole());
        data.put("skillGroup", agent.getSkillGroup());
        data.put("avatar", agent.getAvatar());

        return Result.ok("登录成功", data);
    }

    @PostMapping("/logout")
    @RequireLogin(false)
    public Result<?> logout(HttpSession session) {
        session.invalidate();
        return Result.ok("退出成功");
    }
}
