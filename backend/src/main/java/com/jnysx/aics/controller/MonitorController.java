package com.jnysx.aics.controller;

import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.interceptor.PerformanceInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 系统监控控制器
 * 提供 API 性能指标、错误率统计等监控数据
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
@RequireLogin
public class MonitorController {

    private final PerformanceInterceptor performanceInterceptor;

    /**
     * 获取所有 API 端点的性能指标
     * @return 性能指标列表
     */
    @GetMapping("/performance")
    public Result<List<Map<String, Object>>> getPerformanceMetrics() {
        Map<String, PerformanceInterceptor.PerformanceMetrics> allMetrics = 
            performanceInterceptor.getAllMetrics();
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, PerformanceInterceptor.PerformanceMetrics> entry : allMetrics.entrySet()) {
            String endpoint = entry.getKey();
            PerformanceInterceptor.PerformanceMetrics metrics = entry.getValue();
            
            Map<String, Object> data = new HashMap<>();
            data.put("endpoint", endpoint);
            data.put("requestCount", metrics.getCount());
            data.put("errorCount", metrics.getErrorCount());
            data.put("avgDuration", Math.round(metrics.getAvgDuration() * 100.0) / 100.0);
            data.put("minDuration", metrics.getMinDuration());
            data.put("maxDuration", metrics.getMaxDuration());
            data.put("errorRate", Math.round(metrics.getErrorRate() * 100.0) / 100.0);
            data.put("lastRequestTime", metrics.getLastRequestTime());
            
            result.add(data);
        }
        
        return Result.ok(result);
    }

    /**
     * 获取系统整体健康状态
     * @return 健康状态摘要
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getHealthStatus() {
        Map<String, PerformanceInterceptor.PerformanceMetrics> allMetrics = 
            performanceInterceptor.getAllMetrics();
        
        long totalRequests = 0;
        long totalErrors = 0;
        long totalDuration = 0;
        
        for (PerformanceInterceptor.PerformanceMetrics metrics : allMetrics.values()) {
            totalRequests += metrics.getCount();
            totalErrors += metrics.getErrorCount();
            totalDuration += (long)(metrics.getAvgDuration() * metrics.getCount());
        }
        
        double avgDuration = totalRequests > 0 ? (double) totalDuration / totalRequests : 0;
        double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests * 100 : 0;
        
        Map<String, Object> health = new HashMap<>();
        health.put("totalRequests", totalRequests);
        health.put("totalErrors", totalErrors);
        health.put("avgDuration", Math.round(avgDuration * 100.0) / 100.0);
        health.put("errorRate", Math.round(errorRate * 100.0) / 100.0);
        health.put("status", errorRate > 5 ? "warning" : "healthy");
        health.put("timestamp", System.currentTimeMillis());
        
        return Result.ok(health);
    }
}