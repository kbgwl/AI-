package com.jnysx.aics.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.*;
import com.jnysx.aics.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class DashboardController {

    private final SessionService sessionService;
    private final AgentService agentService;
    private final TicketService ticketService;
    private final UnknownQuestionService unknownQuestionService;
    private final UserService userService;
    private final MessageService messageService;

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        JSONObject data = new JSONObject();

        long totalSessions = sessionService.count();
        long botResolved = sessionService.count(new LambdaQueryWrapper<Session>().eq(Session::getStatus, 3));
        long transferCount = sessionService.count(new LambdaQueryWrapper<Session>().isNotNull(Session::getAgentId));
        long onlineAgents = agentService.count(new LambdaQueryWrapper<Agent>().eq(Agent::getStatus, 1));
        long pendingTickets = ticketService.count(new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, "pending").or().eq(Ticket::getStatus, "processing"));
        long pendingQuestions = unknownQuestionService.count(new LambdaQueryWrapper<UnknownQuestion>().eq(UnknownQuestion::getStatus, 0));
        long totalUsers = userService.count();

        data.put("totalSessions", totalSessions);
        data.put("botResolved", botResolved);
        data.put("transferCount", transferCount);
        data.put("onlineAgents", onlineAgents);
        data.put("pendingTickets", pendingTickets);
        data.put("pendingQuestions", pendingQuestions);
        data.put("totalUsers", totalUsers);

        double rate = totalSessions > 0 ? (double) botResolved / totalSessions * 100 : 0;
        data.put("botResolveRate", String.format("%.1f%%", rate));

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        long todaySessions = sessionService.count(new LambdaQueryWrapper<Session>()
                .ge(Session::getCreateTime, todayStart));
        data.put("todaySessions", todaySessions);

        List<JSONObject> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = sessionService.count(new LambdaQueryWrapper<Session>()
                    .ge(Session::getCreateTime, date.atStartOfDay())
                    .lt(Session::getCreateTime, date.plusDays(1).atStartOfDay()));
            JSONObject point = new JSONObject();
            point.put("date", date.toString());
            point.put("label", getDayLabel(date));
            point.put("count", count);
            trend.add(point);
        }
        data.put("trend", trend);

        long todayBot = sessionService.count(new LambdaQueryWrapper<Session>()
                .eq(Session::getStatus, 3)
                .ge(Session::getCreateTime, todayStart));
        long todayTransfer = sessionService.count(new LambdaQueryWrapper<Session>()
                .isNotNull(Session::getAgentId)
                .ge(Session::getCreateTime, todayStart));
        JSONObject todayRate = new JSONObject();
        todayRate.put("botResolved", todayBot);
        todayRate.put("transferCount", todayTransfer);
        long todayTotal = todayBot + todayTransfer;
        todayRate.put("rate", todayTotal > 0 ? String.format("%.1f%%", (double) todayBot / todayTotal * 100) : "0%");
        data.put("todayResolveRate", todayRate);

        JSONObject funnel = new JSONObject();
        long visitUsers = userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart));
        long convUsers = sessionService.count(new LambdaQueryWrapper<Session>()
                .ge(Session::getCreateTime, todayStart));
        funnel.put("visitUsers", Math.max(visitUsers, convUsers * 2));
        funnel.put("startSession", convUsers);
        funnel.put("botResolved", todayBot);
        funnel.put("transferAgent", todayTransfer);
        data.put("funnel", funnel);

        return Result.ok(data);
    }

    private String getDayLabel(LocalDate date) {
        String[] labels = {"日", "一", "二", "三", "四", "五", "六"};
        return labels[date.getDayOfWeek().getValue() % 7];
    }
}
