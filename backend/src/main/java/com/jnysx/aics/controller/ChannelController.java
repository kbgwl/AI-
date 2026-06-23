package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.ChannelConfig;
import com.jnysx.aics.service.ChannelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 渠道管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class ChannelController {

    private final ChannelConfigService channelConfigService;

    @GetMapping("/channels")
    public Result<?> listChannels() {
        return Result.ok(channelConfigService.list(
            new LambdaQueryWrapper<ChannelConfig>().orderByAsc(ChannelConfig::getId)));
    }

    @PostMapping("/channel")
    public Result<?> addChannel(@RequestBody ChannelConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        channelConfigService.save(config);
        return Result.ok("渠道配置创建成功", config);
    }

    @PutMapping("/channel")
    public Result<?> updateChannel(@RequestBody ChannelConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        channelConfigService.updateById(config);
        return Result.ok("渠道配置更新成功");
    }
}
