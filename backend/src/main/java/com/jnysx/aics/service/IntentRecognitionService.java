package com.jnysx.aics.service;

import com.jnysx.aics.entity.Intent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 智能意图识别服务
 * 基于关键词匹配 + 正则表达式 + 相似度计算
 */
@Service
public class IntentRecognitionService {
    
    @Autowired
    private IntentService intentService;
    
    // 缓存所有 intents 提高性能
    private List<Intent> allIntents = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        loadIntents();
    }
    
    /**
     * 加载意图列表
     */
    public void loadIntents() {
        try {
            allIntents = intentService.list();
        } catch (Exception e) {
            // 数据库未就绪时使用空列表
            allIntents = new ArrayList<>();
        }
    }
    
    /**
     * 识别用户意图
     * @param content 用户输入内容
     * @return 识别结果（意图编码、置信度、匹配类型）
     */
    public IntentResult recognize(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new IntentResult("unknown", 0.0, "empty");
        }
        
        String text = content.toLowerCase().trim();
        
        // 1. 精确匹配意图编码
        for (Intent intent : allIntents) {
            if (intent.getIntentCode().equalsIgnoreCase(text)) {
                return new IntentResult(intent.getIntentCode(), 1.0, "exact");
            }
        }
        
        // 2. 关键词匹配（ samples 中的关键词）
        Map<String, Double> keywordScores = new HashMap<>();
        for (Intent intent : allIntents) {
            String samples = intent.getSamples();
            if (samples != null && samples.startsWith("[")) {
                try {
                    List<String> sampleList = parseSamples(samples);
                    for (String sample : sampleList) {
                        if (text.contains(sample.toLowerCase())) {
                            double score = 0.6 + (sample.length() * 0.05); // 关键词越长分数越高
                            keywordScores.put(intent.getIntentCode(), Math.max(keywordScores.getOrDefault(intent.getIntentCode(), 0.0), score));
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }
        
        if (!keywordScores.isEmpty()) {
            String bestIntent = Collections.max(keywordScores.entrySet(), Map.Entry.comparingByValue()).getKey();
            double confidence = keywordScores.get(bestIntent);
            return new IntentResult(bestIntent, confidence, "keyword");
        }
        
        // 3. 正则表达式匹配（针对特定模式）
        IntentResult regexResult = matchByRegex(text);
        if (regexResult != null) {
            return regexResult;
        }
        
        // 4. 简单相似度匹配（基于字符包含）
        Map<String, Double> similarityScores = new HashMap<>();
        for (Intent intent : allIntents) {
            String intentName = intent.getIntentName().toLowerCase();
            if (text.contains(intentName) || intentName.contains(text)) {
                double score = 0.5 + (Math.min(text.length(), intentName.length()) * 0.02);
                similarityScores.put(intent.getIntentCode(), Math.max(similarityScores.getOrDefault(intent.getIntentCode(), 0.0), score));
            }
        }
        
        if (!similarityScores.isEmpty()) {
            String bestIntent = Collections.max(similarityScores.entrySet(), Map.Entry.comparingByValue()).getKey();
            double confidence = similarityScores.get(bestIntent);
            if (confidence >= 0.5) {
                return new IntentResult(bestIntent, confidence, "similarity");
            }
        }
        
        // 默认：未知意图
        return new IntentResult("unknown", 0.3, "fallback");
    }
    
    /**
     * 正则表达式匹配
     */
    private IntentResult matchByRegex(String text) {
        // 订单查询模式
        if (Pattern.compile("(订单[号？]?|ORD\\d+|下单|购买)").matcher(text).find()) {
            return new IntentResult("query_order", 0.7, "regex");
        }
        
        // 退货退款模式
        if (Pattern.compile("(退货|退款|退钱|不要了|cancelled)").matcher(text).find()) {
            if (text.contains("换") || text.contains("换货")) {
                return new IntentResult("exchange", 0.7, "regex");
            }
            return new IntentResult("refund", 0.7, "regex");
        }
        
        // 转人工模式
        if (Pattern.compile("(人工|真人|客服|电话|投诉|找)").matcher(text).find()) {
            return new IntentResult("transfer_manual", 0.8, "regex");
        }
        
        // 价格咨询模式
        if (Pattern.compile("(价格|多少钱|收费|贵|费用|价目|套餐)").matcher(text).find()) {
            return new IntentResult("price_inquiry", 0.7, "regex");
        }
        
        // 产品咨询模式
        if (Pattern.compile("(产品|功能|服务|支持|能做|可以)").matcher(text).find()) {
            return new IntentResult("product_info", 0.6, "regex");
        }
        
        return null;
    }
    
    /**
     * 解析 samples JSON 字符串
     */
    private List<String> parseSamples(String samplesJson) {
        // 简单解析：去除 [ ] 和引号
        String cleaned = samplesJson.replaceAll("[\\[\\]\"\']", "").trim();
        return Arrays.asList(cleaned.split(","));
    }
    
    /**
     * 意图识别结果
     */
    public static class IntentResult {
        private final String intentCode;
        private final double confidence;
        private final String matchType;
        
        public IntentResult(String intentCode, double confidence, String matchType) {
            this.intentCode = intentCode;
            this.confidence = confidence;
            this.matchType = matchType;
        }
        
        public String getIntentCode() { return intentCode; }
        public double getConfidence() { return confidence; }
        public String getMatchType() { return matchType; }
        
        public boolean isHighConfidence() {
            return confidence >= 0.7;
        }
    }
}