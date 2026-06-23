package com.jnysx.aics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI 对话服务（OkHttp 实现）
 * API Key 优先从数据库读取，支持热更新
 */
@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);
    
    private final String apiUrl;
    private final String model;
    private final int timeout;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final SystemConfigService systemConfigService;
    private String fallbackApiKey;

    public DeepSeekService(
            @Value("${deepseek.api-key:}") String configApiKey,
            @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}") String apiUrl,
            @Value("${deepseek.model:deepseek-chat}") String model,
            @Value("${deepseek.timeout:30}") int timeout,
            SystemConfigService systemConfigService) {
        
        this.systemConfigService = systemConfigService;
        this.apiUrl = apiUrl;
        this.model = model;
        this.timeout = timeout;
        
        // 设置 fallback API Key
        String key = System.getProperty("deepseek.api-key");
        if (key == null || key.trim().isEmpty()) {
            String envKey = System.getenv("DEEPSEEK_API_KEY");
            key = (envKey != null && !envKey.trim().isEmpty()) ? envKey : "";
        }
        if (key == null || key.trim().isEmpty()) {
            key = (configApiKey != null && !configApiKey.trim().isEmpty()) ? configApiKey : "";
        }
        this.fallbackApiKey = key;
        
        String keyDisplay = (this.fallbackApiKey != null && this.fallbackApiKey.length() > 8) 
            ? this.fallbackApiKey.substring(0, 4) + "****" + this.fallbackApiKey.substring(this.fallbackApiKey.length() - 4) 
            : "null/empty";
        log.info("DeepSeekService initialized - fallback apiKey: {}, apiUrl: {}, model: {}", 
            keyDisplay, apiUrl, model);
        
        if (this.fallbackApiKey == null || this.fallbackApiKey.isEmpty()) {
            log.error("DEEPSEEK API KEY IS MISSING! AI chat will not work!");
        }
        
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取当前有效的 API Key（优先从数据库读取）
     */
    private String getApiKey() {
        try {
            String dbKey = systemConfigService.getConfigValue("deepseek.api-key");
            if (dbKey != null && !dbKey.trim().isEmpty()) {
                return dbKey;
            }
        } catch (Exception e) {
            log.warn("Failed to read API key from database, using fallback: {}", e.getMessage());
        }
        return fallbackApiKey;
    }

    /**
     * 发送对话请求（同步）
     */
    public String chat(String userMessage, String systemPrompt, List<Map<String, String>> conversationHistory) {
        try {
            String currentApiKey = getApiKey();
            
            if (currentApiKey == null || currentApiKey.trim().isEmpty()) {
                log.error("API Key is empty!");
                return "抱歉，AI 服务未配置 API Key，请在系统设置中配置。";
            }
            
            Map<String, Object> requestBody = buildRequestBody(userMessage, systemPrompt, conversationHistory);
            String json = objectMapper.writeValueAsString(requestBody);
            
            log.info("Sending request to DeepSeek: {}", userMessage);
            
            RequestBody body = RequestBody.create(
                    json,
                    MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + currentApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("DeepSeek API call failed: {} {}", response.code(), response.message());
                    return "抱歉，AI 服务暂时不可用，请稍后再试。（错误码：" + response.code() + "）";
                }
                
                String responseBody = response.body().string();
                return parseResponse(responseBody);
            }
            
        } catch (IOException e) {
            log.error("DeepSeek API call failed: {}", e.getMessage(), e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 异步对话（需要配合 CompletableFuture 使用）
     */
    public void chatAsync(String userMessage, String systemPrompt, List<Map<String, String>> conversationHistory,
                          java.util.function.Consumer<String> callback) {
        
        new Thread(() -> {
            String response = chat(userMessage, systemPrompt, conversationHistory);
            callback.accept(response);
        }).start();
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String userMessage, String systemPrompt, List<Map<String, String>> conversationHistory) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统提示词
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }
        
        // 历史对话
        if (conversationHistory != null) {
            messages.addAll(conversationHistory);
        }
        
        // 当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        requestBody.put("stream", false);
        
        return requestBody;
    }

    /**
     * 解析响应
     */
    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            log.warn("Invalid response format: {}", responseBody);
            return "抱歉，AI 服务响应格式异常。";
        } catch (Exception e) {
            log.error("Failed to parse DeepSeek response: {}", e.getMessage());
            return "抱歉，AI 服务解析失败。";
        }
    }

    /**
     * 简单对话（无历史）
     */
    public String simpleChat(String userMessage, String systemPrompt) {
        return chat(userMessage, systemPrompt, null);
    }

    @PreDestroy
    public void destroy() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}