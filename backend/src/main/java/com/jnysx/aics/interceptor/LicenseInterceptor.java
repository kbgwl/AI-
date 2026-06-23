package com.jnysx.aics.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Plan;
import com.jnysx.aics.service.LicenseService;
import com.jnysx.aics.service.PlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

@Component
public class LicenseInterceptor implements HandlerInterceptor {

    @Autowired
    private LicenseService licenseService;
    @Autowired
    private PlanService planService;

    private static final Set<String> FREE_URIS = Set.of(
            "/api/admin/login",
            "/api/chat/health"
    );

    private static final Set<String> FREE_URI_PREFIXES = Set.of(
            "/api/license/",
            "/api/chat/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String uri = request.getRequestURI();

        if (FREE_URIS.contains(uri)) {
            return true;
        }

        for (String prefix : FREE_URI_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }

        Map<String, Object> validation = licenseService.validateLicense();
        Boolean valid = (Boolean) validation.get("valid");

        if (valid == null || !valid) {
            if (isBasicFeature(uri)) {
                return true;
            }
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            String msg = (String) validation.getOrDefault("message", "系统未授权");
            response.getWriter().write(JSON.toJSONString(Result.fail(403, "系统授权异常：" + msg)));
            return false;
        }

        String planCode = (String) validation.getOrDefault("plan", "basic");

        Map<String, Boolean> features = loadPlanFeatures(planCode);
        if (!isFeatureAllowed(uri, features)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            String requiredPlan = getRequiredPlan(uri);
            response.getWriter().write(JSON.toJSONString(Result.fail(403, "该功能需要" + requiredPlan + "授权，请升级套餐")));
            return false;
        }

        return true;
    }

    private Map<String, Boolean> loadPlanFeatures(String planCode) {
        try {
            LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Plan::getPlanCode, planCode);
            wrapper.eq(Plan::getStatus, 1);
            Plan plan = planService.getOne(wrapper);
            if (plan != null && plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
                JSONObject json = JSON.parseObject(plan.getFeatures());
                Map<String, Boolean> features = new HashMap<>();
                for (String key : json.keySet()) {
                    features.put(key, json.getBooleanValue(key));
                }
                return features;
            }
        } catch (Exception ignored) {}

        Map<String, Boolean> features = new HashMap<>();
        boolean isBasic = "basic".equals(planCode) || "pro".equals(planCode) || "enterprise".equals(planCode);
        boolean isPro = "pro".equals(planCode) || "enterprise".equals(planCode);
        boolean isEnterprise = "enterprise".equals(planCode);
        features.put("smart_routing", isPro);
        features.put("quality_check", isPro);
        features.put("marketing_rules", isPro);
        features.put("advanced_reports", isPro);
        features.put("multi_tenant", isEnterprise);
        features.put("api_access", isEnterprise);
        features.put("custom_deployment", isEnterprise);
        return features;
    }

    private boolean isFeatureAllowed(String uri, Map<String, Boolean> features) {
        if (uri.startsWith("/api/routing/") || uri.contains("/routing")) {
            return Boolean.TRUE.equals(features.get("smart_routing"));
        }
        if (uri.contains("/quality")) {
            return Boolean.TRUE.equals(features.get("quality_check"));
        }
        if (uri.contains("/marketing")) {
            return Boolean.TRUE.equals(features.get("marketing_rules"));
        }
        if (uri.startsWith("/api/admin/tenant")) {
            return Boolean.TRUE.equals(features.get("multi_tenant"));
        }
        if (uri.contains("/report") && !uri.contains("/admin/report/daily")) {
            return Boolean.TRUE.equals(features.get("advanced_reports"));
        }
        if (uri.contains("/monitor")) {
            return Boolean.TRUE.equals(features.get("api_access"));
        }
        return true;
    }

    private String getRequiredPlan(String uri) {
        if (uri.startsWith("/api/routing/") || uri.contains("/quality") || uri.contains("/marketing")) {
            return "专业版";
        }
        if (uri.startsWith("/api/admin/tenant") || uri.contains("/monitor")) {
            return "企业版";
        }
        return "更高级别";
    }

    private boolean isBasicFeature(String uri) {
        return uri.startsWith("/api/admin/login") ||
               uri.startsWith("/api/admin/dashboard") ||
               uri.startsWith("/api/kb/") ||
               uri.startsWith("/api/admin/intent") ||
               uri.startsWith("/api/admin/agent") ||
               uri.startsWith("/api/admin/channel") ||
               uri.startsWith("/api/admin/flow") ||
               uri.startsWith("/api/ticket/") ||
               uri.startsWith("/api/admin/log");
    }
}
