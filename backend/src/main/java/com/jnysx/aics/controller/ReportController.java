package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.ReportDaily;
import com.jnysx.aics.service.ReportDailyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 数据报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireLogin
public class ReportController {

    private final ReportDailyService reportDailyService;

    @GetMapping("/report/daily")
    public Result<?> dailyReport(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LambdaQueryWrapper<ReportDaily> wrapper = new LambdaQueryWrapper<>();
        if (channel != null) wrapper.eq(ReportDaily::getChannel, channel);
        if (startDate != null) wrapper.ge(ReportDaily::getReportDate, startDate);
        if (endDate != null) wrapper.le(ReportDaily::getReportDate, endDate);
        wrapper.orderByDesc(ReportDaily::getReportDate);
        return Result.ok(reportDailyService.list(wrapper));
    }
}
