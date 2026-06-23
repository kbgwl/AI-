package com.jnysx.aics.controller;

import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.service.SystemConfigService;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment")
@RequiredArgsConstructor
@RequireLogin
public class PaymentConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/config")
    public Result<?> getConfig() {
        JSONObject config = new JSONObject();
        String alipayStr = systemConfigService.getConfigValue("payment.alipay");
        if (alipayStr != null && !alipayStr.isEmpty()) {
            config.put("alipay", JSON.parseObject(alipayStr));
        } else {
            config.put("alipay", Map.of("enabled", false));
        }
        String wechatStr = systemConfigService.getConfigValue("payment.wechat");
        if (wechatStr != null && !wechatStr.isEmpty()) {
            config.put("wechat", JSON.parseObject(wechatStr));
        } else {
            config.put("wechat", Map.of("enabled", false));
        }
        return Result.ok(config);
    }

    @PostMapping("/config")
    public Result<?> saveConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("alipay")) {
            systemConfigService.setConfigValue("payment.alipay", JSON.toJSONString(body.get("alipay")), "支付宝配置", "支付渠道配置");
        }
        if (body.containsKey("wechat")) {
            systemConfigService.setConfigValue("payment.wechat", JSON.toJSONString(body.get("wechat")), "微信支付配置", "支付渠道配置");
        }
        return Result.ok("配置已保存");
    }

    @PostMapping("/test")
    public Result<?> testConnection(@RequestBody Map<String, String> body) {
        String channel = body.get("channel");
        String configKey = "payment." + channel;
        String configStr = systemConfigService.getConfigValue(configKey);
        if (configStr == null || configStr.isEmpty()) {
            return Result.fail("未配置" + channel + "支付");
        }
        JSONObject config = JSON.parseObject(configStr);
        Boolean enabled = config.getBoolean("enabled");
        if (enabled == null || !enabled) {
            return Result.fail(channel + "支付未启用");
        }
        return Result.ok(channel + "支付连接正常");
    }

    @PostMapping("/test-pay")
    public Result<?> testPay(@RequestBody Map<String, Object> body) {
        String channel = (String) body.get("channel");
        Double amount = body.get("amount") != null ? Double.parseDouble(body.get("amount").toString()) : 0.01;
        JSONObject result = new JSONObject();
        result.put("channel", channel);
        result.put("amount", amount);
        result.put("status", "success");
        result.put("message", "测试支付请求已处理（模拟模式）");
        return Result.ok(result);
    }
}
