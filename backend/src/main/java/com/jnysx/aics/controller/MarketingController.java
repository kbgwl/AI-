package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.MarketingRule;
import com.jnysx.aics.service.MarketingRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 营销规则管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class MarketingController {

    private final MarketingRuleService marketingRuleService;

    @GetMapping("/marketing/rules")
    public Result<?> listMarketingRules() {
        return Result.ok(marketingRuleService.list(
            new LambdaQueryWrapper<MarketingRule>()
                .eq(MarketingRule::getStatus, 1)
                .orderByDesc(MarketingRule::getPriority)));
    }

    @PostMapping("/marketing/rule")
    public Result<?> addMarketingRule(@RequestBody MarketingRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setDailyCount(0);
        marketingRuleService.save(rule);
        return Result.ok("营销规则创建成功", rule);
    }
}
