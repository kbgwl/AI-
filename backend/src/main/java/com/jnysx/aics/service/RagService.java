package com.jnysx.aics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * RAG (Retrieval-Augmented Generation) 服务
 * 基于知识库进行智能检索和增强生成
 */
@Service
public class RagService {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient client;
    
    @Value("${deepseek.api-key:}")
    private String deepSeekApiKey;
    
    @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String deepSeekApiUrl;
    
    @Value("${rag.top-k:5}")
    private int topK;
    
    @Value("${rag.similarity-threshold:0.6}")
    private double similarityThreshold;
    
    @PostConstruct
    public void init() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 检索知识库，返回最相关的文档片段
     * @param query 用户问题
     * @param kbItems 知识库条目列表
     * @return 相关的文档片段
     */
    public List<KbSearchResult> search(String query, List<KbItem> kbItems) {
        if (kbItems == null || kbItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 计算关键词匹配度（TF-IDF 简化版）
        for (KbItem item : kbItems) {
            double keywordScore = calculateKeywordScore(query, item);
            item.setScore(keywordScore);
        }
        
        // 2. 按分数排序
        kbItems.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // 3. 取 Top-K
        List<KbItem> topItems = kbItems.stream()
                .filter(item -> item.getScore() >= similarityThreshold)
                .limit(topK)
                .toList();
        
        // 4. 使用 DeepSeek 进行语义重排序（可选，如果 API 可用）
        if (deepSeekApiKey != null && !deepSeekApiKey.isEmpty() && !topItems.isEmpty()) {
            try {
                topItems = rerankWithAI(query, topItems);
            } catch (Exception e) {
                // AI 重排失败，使用关键词排序结果
            }
        }
        
        // 5. 转换为搜索结果
        List<KbSearchResult> results = new ArrayList<>();
        for (KbItem item : topItems) {
            results.add(new KbSearchResult(
                item.getId(),
                item.getTitle(),
                item.getContent(),
                item.getCategory(),
                item.getScore()
            ));
        }
        
        return results;
    }
    
    /**
     * 计算关键词匹配分数
     */
    private double calculateKeywordScore(String query, KbItem item) {
        // 分词
        Set<String> queryWords = tokenize(query);
        Set<String> titleWords = tokenize(item.getTitle());
        Set<String> contentWords = tokenize(item.getContent());
        
        // 标题匹配权重更高
        int titleMatch = 0;
        for (String word : queryWords) {
            if (titleWords.contains(word)) {
                titleMatch++;
            }
        }
        
        // 内容匹配
        int contentMatch = 0;
        for (String word : queryWords) {
            if (contentWords.contains(word)) {
                contentMatch++;
            }
        }
        
        // 计算分数：标题匹配权重 0.7，内容匹配权重 0.3
        double titleScore = (double) titleMatch / Math.max(queryWords.size(), 1);
        double contentScore = (double) contentMatch / Math.max(queryWords.size(), 1);
        
        return 0.7 * titleScore + 0.3 * contentScore;
    }
    
    /**
     * 简单分词：中文按字，英文按空格
     */
    private Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return words;
        }
        
        // 移除标点符号
        text = text.replaceAll("[\\p{P}\\p{S}]", " ").toLowerCase();
        
        // 中文按字分词，英文按空格
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == ' ') {
                String[] parts = text.split("\\s+");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        words.add(part.trim());
                    }
                }
                break;
            }
        }
        
        // 如果没有英文，按字分词
        if (words.isEmpty()) {
            for (char c : text.trim().toCharArray()) {
                if (c != ' ') {
                    words.add(String.valueOf(c));
                }
            }
        }
        
        return words;
    }
    
    /**
     * 使用 DeepSeek 进行语义重排序
     */
    private List<KbItem> rerankWithAI(String query, List<KbItem> items) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能客服助手。请根据用户问题，对以下知识库条线的相对相关性进行排序。\n\n");
        prompt.append("用户问题：").append(query).append("\n\n");
        prompt.append("知识库条目：\n");
        
        for (int i = 0; i < items.size(); i++) {
            KbItem item = items.get(i);
            prompt.append(String.format("[%d] 标题：%s\n内容：%s\n\n", i, item.getTitle(), item.getContent()));
        }
        
        prompt.append("请只返回排序后的索引列表，格式如：[2, 0, 1]");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt.toString());
        messages.add(userMsg);
        requestBody.put("messages", messages);
        
        String json = objectMapper.writeValueAsString(requestBody);
        
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(deepSeekApiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + deepSeekApiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return items; // 返回原顺序
            }
            
            String responseBody = response.body().string();
            // 解析 DeepSeek 返回的排序结果（简化处理）
            // 实际实现需要解析 JSON 并提取排序索引
            
        }
        
        return items; // 暂时返回原顺序
    }
    
    /**
     * 生成基于知识库的增强回复
     */
    public String generateAnswer(String query, List<KbSearchResult> context, String originalAiResponse) {
        if (context.isEmpty()) {
            return originalAiResponse;
        }
        
        StringBuilder contextText = new StringBuilder();
        for (int i = 0; i < Math.min(context.size(), 3); i++) {
            KbSearchResult result = context.get(i);
            contextText.append(String.format("【参考%d】%s: %s\n", 
                i + 1, result.getTitle(), result.getContent()));
        }
        
        return String.format("根据知识库信息，%s\n\n💡 参考资料:\n%s", 
            originalAiResponse, contextText.toString());
    }
    
    // ==================== 数据结构 ====================
    
    public static class KbItem {
        private Long id;
        private String title;
        private String content;
        private String category;
        private double score;
        
        public KbItem() {}
        
        public KbItem(Long id, String title, String content, String category) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.category = category;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
    
    public static class KbSearchResult {
        private final Long id;
        private final String title;
        private final String content;
        private final String category;
        private final double relevanceScore;
        
        public KbSearchResult(Long id, String title, String content, String category, double relevanceScore) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.category = category;
            this.relevanceScore = relevanceScore;
        }
        
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
        public double getRelevanceScore() { return relevanceScore; }
    }
}
