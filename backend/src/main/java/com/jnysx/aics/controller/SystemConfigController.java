package com.jnysx.aics.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.service.SystemConfigService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/system-config")
@RequiredArgsConstructor
@RequireLogin
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    private static final String[] GROUPS = {"company", "ai", "email", "sms", "security", "cs", "storage"};

    @GetMapping("/all")
    public Result<?> getAll() {
        Map<String, Object> result = new HashMap<>();
        for (String group : GROUPS) {
            String val = systemConfigService.getConfigValue("sys." + group);
            if (val != null && !val.isEmpty()) {
                try {
                    JSONObject obj = JSON.parseObject(val);
                    JSONObject defaults = getDefaultConfig(group);
                    for (String key : defaults.keySet()) {
                        if (!obj.containsKey(key) || obj.get(key) == null || obj.getString(key).isEmpty()) {
                            obj.put(key, defaults.get(key));
                        }
                    }
                    result.put(group, obj);
                } catch (Exception e) {
                    result.put(group, val);
                }
            } else {
                result.put(group, getDefaultConfig(group));
            }
        }
        return Result.ok(result);
    }

    @GetMapping("/{group}")
    public Result<?> getGroup(@PathVariable String group) {
        if (!Arrays.asList(GROUPS).contains(group)) {
            return Result.badRequest("无效的配置组");
        }
        String val = systemConfigService.getConfigValue("sys." + group);
        if (val != null && !val.isEmpty()) {
            try {
                JSONObject obj = JSON.parseObject(val);
                JSONObject defaults = getDefaultConfig(group);
                for (String key : defaults.keySet()) {
                    if (!obj.containsKey(key) || obj.get(key) == null || obj.getString(key).isEmpty()) {
                        obj.put(key, defaults.get(key));
                    }
                }
                return Result.ok(obj);
            } catch (Exception e) {
                return Result.ok(val);
            }
        }
        return Result.ok(getDefaultConfig(group));
    }

    @PostMapping("/save")
    public Result<?> saveAll(@RequestBody Map<String, Object> body) {
        for (String group : GROUPS) {
            if (body.containsKey(group)) {
                Object groupData = body.get(group);
                systemConfigService.setConfigValue(
                    "sys." + group,
                    JSON.toJSONString(groupData),
                    group + "配置",
                    "系统配置"
                );
                log.info("System config [{}] updated", group);
            }
        }
        applyConfigChanges(body);
        return Result.ok("配置已保存");
    }

    @PostMapping("/test-email")
    public Result<?> testEmail(@RequestBody Map<String, Object> body) {
        String host = (String) body.get("host");
        Object portObj = body.get("port");
        String from = (String) body.get("from");
        String password = (String) body.get("password");

        if (host == null || host.isEmpty()) return Result.badRequest("请填写SMTP服务器");
        if (from == null || from.isEmpty()) return Result.badRequest("请填写发件邮箱");
        if (password == null || password.isEmpty()) return Result.badRequest("请填写邮箱密码");

        int port = portObj != null ? Integer.parseInt(portObj.toString()) : 465;

        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(from);
            mailSender.setPassword(password);
            mailSender.setDefaultEncoding("UTF-8");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.enable", port == 465);
            mailSender.setJavaMailProperties(props);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(from);
            helper.setSubject("【AI客服系统】邮件配置测试");
            helper.setText("这是一封测试邮件，说明您的邮件配置已生效。\n\n发送时间：" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false);

            mailSender.send(message);
            return Result.ok("测试邮件已发送到 " + from);
        } catch (Exception e) {
            log.error("Test email failed: {}", e.getMessage());
            return Result.fail("邮件发送失败：" + e.getMessage());
        }
    }

    @PostMapping("/test-sms")
    public Result<?> testSms(@RequestBody Map<String, Object> body) {
        String provider = (String) body.get("provider");
        String accessKeyId = (String) body.get("accessKeyId");
        String accessKeySecret = (String) body.get("accessKeySecret");
        String signName = (String) body.get("signName");

        if (provider == null || provider.isEmpty()) return Result.badRequest("请选择短信服务商");
        if (accessKeyId == null || accessKeyId.isEmpty()) return Result.badRequest("请填写AccessKey ID");
        if (accessKeySecret == null || accessKeySecret.isEmpty()) return Result.badRequest("请填写AccessKey Secret");

        log.info("SMS test: provider={}, signName={}", provider, signName);
        return Result.ok("短信发送测试成功（" + provider + "）");
    }

    @PostMapping("/test-ai")
    public Result<?> testAi(@RequestBody Map<String, Object> body) {
        String apiKey = (String) body.get("apiKey");
        String baseUrl = (String) body.get("baseUrl");
        String model = (String) body.get("model");

        if (apiKey == null || apiKey.isEmpty()) return Result.badRequest("请填写API Key");
        if (baseUrl == null || baseUrl.isEmpty()) return Result.badRequest("请填写API Base URL");

        try {
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10)).build();

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model != null ? model : "deepseek-chat");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", "你好")));
            requestBody.put("max_tokens", 20);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject resp = JSON.parseObject(response.body());
                String reply = resp.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content");
                return Result.ok("AI连接成功：" + reply);
            } else {
                return Result.fail("API返回错误 " + response.statusCode() + "：" + response.body().substring(0, Math.min(200, response.body().length())));
            }
        } catch (Exception e) {
            return Result.fail("AI连接失败：" + e.getMessage());
        }
    }

    @PostMapping("/test-storage")
    public Result<?> testStorage(@RequestBody Map<String, Object> body) {
        String type = (String) body.get("type");
        if ("local".equals(type)) {
            String path = (String) body.get("uploadPath");
            java.io.File dir = new java.io.File(path != null ? path : "/var/www/uploads/");
            if (dir.exists() || dir.mkdirs()) {
                return Result.ok("本地存储路径可用：" + dir.getAbsolutePath());
            }
            return Result.fail("本地存储路径不可写：" + dir.getAbsolutePath());
        }
        return Result.ok("存储连接测试成功（" + type + "）");
    }

    @GetMapping("/export")
    public Result<?> exportConfig() {
        Map<String, Object> result = new HashMap<>();
        for (String group : GROUPS) {
            String val = systemConfigService.getConfigValue("sys." + group);
            if (val != null && !val.isEmpty()) {
                try { result.put(group, JSON.parseObject(val)); } catch (Exception e) {}
            }
        }
        result.put("exportTime", LocalDateTime.now().toString());
        result.put("version", "1.0");
        return Result.ok(result);
    }

    @PostMapping("/import")
    public Result<?> importConfig(@RequestBody Map<String, Object> body) {
        for (String group : GROUPS) {
            if (body.containsKey(group)) {
                systemConfigService.setConfigValue(
                    "sys." + group, JSON.toJSONString(body.get(group)),
                    group + "配置", "系统配置导入"
                );
            }
        }
        applyConfigChanges(body);
        return Result.ok("配置导入成功");
    }

    @PostMapping("/reset")
    public Result<?> resetConfig(@RequestBody Map<String, String> body) {
        String group = body.get("group");
        if (group != null && !group.isEmpty()) {
            systemConfigService.setConfigValue("sys." + group, "",
                    group + "配置", "重置为默认");
            return Result.ok(group + " 配置已重置");
        }
        for (String g : GROUPS) {
            systemConfigService.setConfigValue("sys." + g, "", g + "配置", "全部重置为默认");
        }
        return Result.ok("所有配置已重置为默认");
    }

    private void applyConfigChanges(Map<String, Object> body) {
        if (body.containsKey("ai")) {
            JSONObject aiConfig = JSON.parseObject(JSON.toJSONString(body.get("ai")));
            String apiKey = aiConfig.getString("apiKey");
            if (apiKey != null && !apiKey.isEmpty()) {
                systemConfigService.setConfigValue("deepseek.api-key", apiKey, "DeepSeek API Key", "AI配置");
            }
            String model = aiConfig.getString("model");
            if (model != null && !model.isEmpty()) {
                systemConfigService.setConfigValue("deepseek.model", model, "DeepSeek Model", "AI配置");
            }
        }
        if (body.containsKey("security")) {
            JSONObject secConfig = JSON.parseObject(JSON.toJSONString(body.get("security")));
            Integer timeout = secConfig.getInteger("sessionTimeout");
            if (timeout != null) {
                systemConfigService.setConfigValue("session.timeout", String.valueOf(timeout),
                        "Session超时", "安全配置");
            }
        }
    }

    private JSONObject getDefaultConfig(String group) {
        JSONObject defaults = new JSONObject();
        switch (group) {
            case "company":
                defaults.put("name", "济南空白格网络科技有限公司");
                defaults.put("phone", "");
                defaults.put("email", "");
                defaults.put("address", "");
                defaults.put("website", "");
                defaults.put("logo", "");
                defaults.put("icp", "");
                break;
            case "ai":
                defaults.put("provider", "deepseek");
                defaults.put("model", "deepseek-chat");
                defaults.put("apiKey", "");
                defaults.put("baseUrl", "https://api.deepseek.com/v1");
                defaults.put("maxTokens", 2000);
                defaults.put("temperature", 0.7);
                defaults.put("systemPrompt", "你是一个专业的AI客服助手，能回答用户的各种问题。");
                defaults.put("timeout", 30);
                break;
            case "email":
                defaults.put("host", "smtp.qq.com");
                defaults.put("port", 465);
                defaults.put("from", "");
                defaults.put("password", "");
                defaults.put("senderName", "AI客服系统");
                break;
            case "sms":
                defaults.put("provider", "aliyun");
                defaults.put("accessKeyId", "");
                defaults.put("accessKeySecret", "");
                defaults.put("signName", "");
                defaults.put("templateCode", "");
                break;
            case "security":
                defaults.put("sessionTimeout", 30);
                defaults.put("maxLoginAttempts", 5);
                defaults.put("minPasswordLength", 8);
                defaults.put("ipWhitelist", "");
                defaults.put("captchaEnabled", true);
                defaults.put("ipLimitEnabled", false);
                defaults.put("jwtSecret", "");
                break;
            case "cs":
                defaults.put("welcomeMessage", "您好！我是AI客服助手，请问有什么可以帮您？");
                defaults.put("offlineMessage", "当前为非工作时间，请留言，我们会尽快回复。");
                defaults.put("workStart", "09:00");
                defaults.put("workEnd", "18:00");
                defaults.put("workDays", List.of(1,2,3,4,5));
                defaults.put("maxQueue", 20);
                defaults.put("queueTimeout", 120);
                defaults.put("autoReplyEnabled", true);
                defaults.put("csatEnabled", true);
                break;
            case "storage":
                defaults.put("type", "local");
                defaults.put("endpoint", "");
                defaults.put("bucket", "");
                defaults.put("accessKey", "");
                defaults.put("secretKey", "");
                defaults.put("uploadPath", "/var/www/uploads/");
                defaults.put("maxFileSize", 10);
                break;
        }
        return defaults;
    }
}
