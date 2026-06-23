package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.OperationLog;
import com.jnysx.aics.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class LogController {

    private final OperationLogService operationLogService;

    @GetMapping("/logs")
    public Result<?> listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(required = false) String module) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null) wrapper.eq(OperationLog::getModule, module);
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return Result.ok(operationLogService.page(new Page<>(page, size), wrapper));
    }
}
