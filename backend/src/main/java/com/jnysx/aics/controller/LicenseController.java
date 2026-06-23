package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.License;
import com.jnysx.aics.entity.Plan;
import com.jnysx.aics.service.LicenseService;
import com.jnysx.aics.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;
    @Autowired
    private PlanService planService;

    @GetMapping("/machine-code")
    public Result<Map<String, String>> getMachineCode() {
        return Result.ok(Map.of("machineCode", licenseService.getMachineCode()));
    }

    @PostMapping("/activate")
    public Result<?> activate(@RequestBody Map<String, String> body) {
        String licenseKey = body.get("licenseKey");
        String machineCode = body.get("machineCode");
        if (licenseKey == null || licenseKey.isEmpty()) {
            return Result.badRequest("请输入授权码");
        }
        if (machineCode == null || machineCode.isEmpty()) {
            machineCode = licenseService.getMachineCode();
        }
        boolean ok = licenseService.activateLicense(licenseKey, machineCode);
        if (ok) {
            return Result.ok("激活成功");
        }
        return Result.fail("激活失败，请检查授权码是否正确");
    }

    @GetMapping("/validate")
    public Result<Map<String, Object>> validate() {
        return Result.ok(licenseService.validateLicense());
    }

    @RequireLogin
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo() {
        return Result.ok(licenseService.getLicenseInfo());
    }

    @GetMapping("/features")
    public Result<Map<String, Object>> getFeatures() {
        Map<String, Object> validation = licenseService.validateLicense();
        Boolean valid = (Boolean) validation.get("valid");
        String planCode = (String) validation.getOrDefault("plan", "none");

        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("plan", planCode);

        Map<String, Boolean> features = loadFeaturesFromPlan(planCode, Boolean.TRUE.equals(valid));
        result.put("features", features);
        result.put("maxAgents", validation.get("maxAgents"));
        result.put("maxSessions", validation.get("maxSessions"));
        result.put("expireTime", validation.get("expireTime"));
        return Result.ok(result);
    }

    @RequireLogin
    @PostMapping("/generate")
    public Result<?> generate(@RequestBody Map<String, Object> body) {
        String customerName = (String) body.getOrDefault("customerName", "Customer");
        String planType = (String) body.getOrDefault("planType", "basic");
        int durationDays = (int) body.getOrDefault("durationDays", 365);
        int maxAgents = (int) body.getOrDefault("maxAgents", 5);

        String key = licenseService.generateLicenseKey(customerName, planType, durationDays, maxAgents);

        License license = new License();
        license.setLicenseKey(key);
        license.setCustomerName(customerName);
        license.setPlanType(planType);
        license.setMaxAgents(maxAgents);
        license.setMaxSessions(planType.equals("enterprise") ? 1000 : planType.equals("pro") ? 500 : 100);
        license.setLicenseType(planType.equals("lifetime") ? "lifetime" : "time");
        license.setStatus(0);
        licenseService.save(license);

        return Result.ok(Map.of("licenseKey", key, "customerName", customerName, "planType", planType));
    }

    @GetMapping("/plans")
    public Result<?> getPlans() {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getStatus, 1);
        wrapper.orderByAsc(Plan::getSort);
        return Result.ok(planService.list(wrapper));
    }

    private Map<String, Boolean> loadFeaturesFromPlan(String planCode, boolean valid) {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getPlanCode, planCode);
        wrapper.eq(Plan::getStatus, 1);
        Plan plan = planService.getOne(wrapper);

        if (plan != null && plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
            try {
                JSONObject json = JSON.parseObject(plan.getFeatures());
                Map<String, Boolean> features = new LinkedHashMap<>();
                for (String key : json.keySet()) {
                    features.put(key, json.getBooleanValue(key) && valid);
                }
                features.put("login", true);
                features.put("license_manage", true);
                features.put("ai_chat", true);
                return features;
            } catch (Exception ignored) {}
        }

        Map<String, Boolean> features = new LinkedHashMap<>();
        boolean isBasic = valid && ("basic".equals(planCode) || "pro".equals(planCode) || "enterprise".equals(planCode));
        boolean isPro = valid && ("pro".equals(planCode) || "enterprise".equals(planCode));
        boolean isEnterprise = valid && "enterprise".equals(planCode);

        features.put("login", true);
        features.put("license_manage", true);
        features.put("ai_chat", true);
        features.put("chat_history", isBasic);
        features.put("dashboard", isBasic);
        features.put("knowledge_base", isBasic);
        features.put("intent_manage", isBasic);
        features.put("agent_manage", isBasic);
        features.put("channel_manage", isBasic);
        features.put("dialog_flow", isBasic);
        features.put("ticket_manage", isBasic);
        features.put("operation_log", isBasic);
        features.put("smart_routing", isPro);
        features.put("quality_check", isPro);
        features.put("marketing_rules", isPro);
        features.put("advanced_reports", isPro);
        features.put("multi_tenant", isEnterprise);
        features.put("api_access", isEnterprise);
        features.put("custom_deployment", isEnterprise);

        return features;
    }
}
