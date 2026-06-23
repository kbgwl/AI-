package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Intent;
import com.jnysx.aics.service.IntentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 意图管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class IntentController {

    private final IntentService intentService;

    @GetMapping("/intents")
    public Result<?> listIntents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Intent> wrapper = new LambdaQueryWrapper<>();
        if (category != null) wrapper.eq(Intent::getCategory, category);
        wrapper.orderByDesc(Intent::getPriority);
        Page<Intent> result = intentService.page(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @PostMapping("/intent")
    public Result<?> addIntent(@RequestBody Intent intent) {
        intent.setCreateTime(LocalDateTime.now());
        intent.setUpdateTime(LocalDateTime.now());
        intentService.save(intent);
        return Result.ok("意图创建成功", intent);
    }

    @PutMapping("/intent")
    public Result<?> updateIntent(@RequestBody Intent intent) {
        intent.setUpdateTime(LocalDateTime.now());
        intentService.updateById(intent);
        return Result.ok("意图更新成功");
    }

    @DeleteMapping("/intent/{id}")
    public Result<?> deleteIntent(@PathVariable Long id) {
        intentService.removeById(id);
        return Result.ok("意图删除成功");
    }
}
