package com.jnysx.aics.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能监控拦截器 - 收集 API 响应时间和错误率
 */
@Component
public class PerformanceInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
    
    // 存储每个 API 端点的性能数据
    private static final Map<String, PerformanceMetrics> metricsMap = new ConcurrentHashMap<>();
    
    // 请求开始时间属性名
    private static final String START_TIME_ATTR = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        String endpoint = getEndpointPath(request);
        int status = response.getStatus();
        
        // 更新指标
        updateMetrics(endpoint, duration, status);
        
        // 记录慢请求（>1000ms）
        if (duration > 1000) {
            logger.warn("慢请求：{} - {}ms, 状态：{}", endpoint, duration, status);
        }
        
        // 记录错误
        if (ex != null || status >= 400) {
            logger.error("请求错误：{} - 状态：{}, 异常：{}", 
                endpoint, status, ex != null ? ex.getMessage() : "无");
        }
    }
    
    /**
     * 获取标准化的端点路径
     */
    private String getEndpointPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 移除查询参数
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        // 规范化路径（移除 ID 等变量）
        path = path.replaceAll("/\\d+", "/{id}");
        return path;
    }
    
    /**
     * 更新性能指标
     */
    private void updateMetrics(String endpoint, long duration, int status) {
        PerformanceMetrics metrics = metricsMap.computeIfAbsent(endpoint, k -> new PerformanceMetrics());
        metrics.recordRequest(duration, status);
    }
    
    /**
     * 获取所有端点的性能指标（供监控接口调用）
     */
    public Map<String, PerformanceMetrics> getAllMetrics() {
        return new HashMap<>(metricsMap);
    }
    
    /**
     * 单个端点的性能指标
     */
    public static class PerformanceMetrics {
        private long count = 0;
        private long errorCount = 0;
        private long totalDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private long maxDuration = 0;
        private long lastRequestTime = 0;
        
        public synchronized void recordRequest(long duration, int status) {
            count++;
            totalDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
            lastRequestTime = System.currentTimeMillis();
            
            if (status >= 400) {
                errorCount++;
            }
        }
        
        public long getCount() {
            return count;
        }
        
        public long getErrorCount() {
            return errorCount;
        }
        
        public double getAvgDuration() {
            return count > 0 ? (double) totalDuration / count : 0;
        }
        
        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }
        
        public long getMaxDuration() {
            return maxDuration;
        }
        
        public double getErrorRate() {
            return count > 0 ? (double) errorCount / count * 100 : 0;
        }
        
        public long getLastRequestTime() {
            return lastRequestTime;
        }
    }
}