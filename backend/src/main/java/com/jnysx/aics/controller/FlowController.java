package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.DialogFlow;
import com.jnysx.aics.service.DialogFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话流程管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class FlowController {

    private final DialogFlowService dialogFlowService;

    @GetMapping("/flows")
    public Result<?> listFlows() {
        return Result.ok(dialogFlowService.list(
            new LambdaQueryWrapper<DialogFlow>().orderByAsc(DialogFlow::getId)));
    }

    @PostMapping("/flow")
    public Result<?> addFlow(@RequestBody DialogFlow flow) {
        flow.setCreateTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        dialogFlowService.save(flow);
        return Result.ok("对话流程创建成功", flow);
    }

    @PutMapping("/flow")
    public Result<?> updateFlow(@RequestBody DialogFlow flow) {
        flow.setUpdateTime(LocalDateTime.now());
        dialogFlowService.updateById(flow);
        return Result.ok("对话流程更新成功");
    }
}
