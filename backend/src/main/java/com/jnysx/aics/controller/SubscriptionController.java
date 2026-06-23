package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Subscription;
import com.jnysx.aics.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subscription")
@RequiredArgsConstructor
@RequireLogin
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long tenantId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Subscription::getTenantId, tenantId);
        wrapper.orderByDesc(Subscription::getCreateTime);
        return Result.ok(subscriptionService.list(wrapper));
    }

    @GetMapping("/active/{tenantId}")
    public Result<?> getActive(@PathVariable Long tenantId) {
        Subscription sub = subscriptionService.lambdaQuery()
                .eq(Subscription::getTenantId, tenantId)
                .eq(Subscription::getStatus, "active")
                .one();
        return Result.ok(sub);
    }
}
