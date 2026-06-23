package com.jnysx.aics.controller;

import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Message;
import com.jnysx.aics.entity.Session;
import com.jnysx.aics.service.ChatOrchestrator;
import com.jnysx.aics.service.InputSanitizer;
import com.jnysx.aics.service.IntentRecognitionService;
import com.jnysx.aics.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 对话控制器（薄适配器，委托给 ChatOrchestrator）
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"https://ai-cs.jnysx.cn", "http://localhost:5174", "http://127.0.0.1:5174"}, allowedHeaders = "*", allowCredentials = "true")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatOrchestrator chatOrchestrator;
    private final IntentRecognitionService intentRecognitionService;
    private final InputSanitizer inputSanitizer;
    private final SessionService sessionService;

    public ChatController(ChatOrchestrator chatOrchestrator,
                          IntentRecognitionService intentRecognitionService,
                          InputSanitizer inputSanitizer,
                          SessionService sessionService) {
        this.chatOrchestrator = chatOrchestrator;
        this.intentRecognitionService = intentRecognitionService;
        this.inputSanitizer = inputSanitizer;
        this.sessionService = sessionService;
    }

    /**
     * AI 对话接口
     */
    @PostMapping("/ai")
    public Result<Map<String, Object>> aiChat(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = request.get("sessionId") != null ? request.get("sessionId").toString() : null;
            String userMessage = (String) request.get("message");
            Boolean useRobot = (Boolean) request.get("useRobot");

            // 参数校验
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.fail("sessionId 不能为空");
            }
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Result.fail("消息内容不能为空");
            }

            sessionId = sessionId.trim();
            if (sessionId.length() > 64) {
                return Result.fail("sessionId 过长");
            }

            userMessage = userMessage.trim();
            if (userMessage.length() > 1000) {
                log.warn("AI chat input too long: {} chars", userMessage.length());
                return Result.fail("消息内容过长，请控制在 1000 字以内");
            }

            userMessage = inputSanitizer.sanitize(userMessage);
            if (inputSanitizer.containsSqlInjection(userMessage)) {
                log.warn("Potential SQL injection in AI chat: {}", userMessage);
                return Result.fail("输入包含非法字符");
            }

            if (!sessionId.matches("^[a-zA-Z0-9\\-]+$")) {
                log.warn("Invalid sessionId format: {}", sessionId);
                return Result.fail("sessionId 格式不正确");
            }

            Map<String, Object> response = chatOrchestrator.processMessage(sessionId, userMessage, useRobot);
            return Result.ok(response);

        } catch (Exception e) {
            log.error("AI chat failed: {}", e.getMessage(), e);
            return Result.fail("AI 对话失败：" + e.getMessage());
        }
    }

    /**
     * 切换机器人/人工模式
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleRobotMode(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = request.get("sessionId").toString();
            Boolean useRobot = (Boolean) request.get("useRobot");

            if (sessionId == null || useRobot == null) {
                return Result.fail("参数错误");
            }

            log.info("Session {} switched to {} mode", sessionId, useRobot ? "ROBOT" : "HUMAN_AGENT");

            Map<String, Object> response = new HashMap<>();
            response.put("useRobot", useRobot);
            response.put("message", useRobot ? "已切换到机器人客服模式" : "已切换到人工客服模式");

            return Result.ok(response);

        } catch (Exception e) {
            log.error("Toggle mode failed: {}", e.getMessage(), e);
            return Result.fail("切换模式失败：" + e.getMessage());
        }
    }

    /**
     * 转接人工客服
     */
    @PostMapping("/transfer")
    public Result<Map<String, Object>> transferToAgent(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = request.get("sessionId") != null ? request.get("sessionId").toString() : null;
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.fail("sessionId 不能为空");
            }
            
            Map<String, Object> result = chatOrchestrator.transferToAgent(sessionId.trim());
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.ok(result);
            } else {
                return Result.fail((String) result.get("message"));
            }
            
        } catch (Exception e) {
            log.error("Transfer to agent failed: {}", e.getMessage(), e);
            return Result.fail("转接失败：" + e.getMessage());
        }
    }

    /**
     * 客服发送消息
     */
    @PostMapping("/agent-reply")
    public Result<Map<String, Object>> agentReply(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = request.get("sessionId") != null ? request.get("sessionId").toString() : null;
            String agentIdStr = request.get("agentId") != null ? request.get("agentId").toString() : null;
            String content = request.get("content") != null ? request.get("content").toString() : null;
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.fail("sessionId 不能为空");
            }
            if (agentIdStr == null || agentIdStr.trim().isEmpty()) {
                return Result.fail("agentId 不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return Result.fail("消息内容不能为空");
            }
            
            Long agentId = Long.parseLong(agentIdStr);
            Map<String, Object> result = chatOrchestrator.agentSendMessage(sessionId.trim(), agentId, content.trim());
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.ok(result);
            } else {
                return Result.fail((String) result.get("message"));
            }
            
        } catch (Exception e) {
            log.error("Agent reply failed: {}", e.getMessage(), e);
            return Result.fail("发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/session-messages")
    public Result<List<Message>> getSessionMessages(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.fail("sessionId 不能为空");
            }
            
            List<Message> messages = chatOrchestrator.getSessionMessages(sessionId.trim(), limit);
            return Result.ok(messages);
            
        } catch (Exception e) {
            log.error("Get session messages failed: {}", e.getMessage(), e);
            return Result.fail("获取消息失败：" + e.getMessage());
        }
    }

    /**
     * 意图识别测试接口
     */
    @PostMapping("/intent")
    public Result<Map<String, Object>> recognizeIntent(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");

            if (content == null || content.trim().isEmpty()) {
                return Result.fail("内容为空");
            }

            content = content.trim();
            if (content.length() > 500) {
                log.warn("Intent recognition input too long: {} chars", content.length());
                return Result.fail("输入内容过长，请控制在 500 字以内");
            }

            content = inputSanitizer.sanitize(content);
            if (inputSanitizer.containsSqlInjection(content)) {
                log.warn("Potential SQL injection detected: {}", content);
                return Result.fail("输入包含非法字符");
            }

            IntentRecognitionService.IntentResult result = intentRecognitionService.recognize(content);

            Map<String, Object> response = new HashMap<>();
            response.put("intentCode", result.getIntentCode());
            response.put("confidence", result.getConfidence());
            response.put("matchType", result.getMatchType());
            response.put("isHighConfidence", result.isHighConfidence());
            response.put("message", getIntentMessage(result.getIntentCode()));

            return Result.ok(response);

        } catch (Exception e) {
            log.error("Intent recognition failed: {}", e.getMessage(), e);
            return Result.fail("意图识别失败：" + e.getMessage());
        }
    }

    private String getIntentMessage(String intentCode) {
        return switch (intentCode) {
            case "query_order" -> "识别为订单查询意图";
            case "refund" -> "识别为退款咨询意图";
            case "exchange" -> "识别为换货申请意图";
            case "transfer_manual" -> "识别为转人工意图";
            case "complaint" -> "识别为投诉建议意图";
            case "product_info" -> "识别为产品咨询意图";
            case "price_inquiry" -> "识别为价格咨询意图";
            default -> "暂未识别到明确意图";
        };
    }

    /**
     * 创建会话
     */
    @PostMapping("/session")
    public Result<Map<String, Object>> createSession(@RequestBody Map<String, Object> request) {
        try {
            String channel = request.get("channel") != null ? request.get("channel").toString() : "web";
            
            // 生成会话ID
            String sessionId = "ws-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            
            // 创建会话记录
            com.jnysx.aics.entity.Session session = new com.jnysx.aics.entity.Session();
            session.setSessionId(sessionId);
            session.setChannel(channel);
            session.setStatus(0); // 机器人对话中
            session.setMode(0); // AI自动
            session.setStartedAt(java.time.LocalDateTime.now());
            session.setCreateTime(java.time.LocalDateTime.now());
            session.setUpdateTime(java.time.LocalDateTime.now());
            session.setDeleted(0);
            
            sessionService.save(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("channel", channel);
            response.put("message", "会话创建成功");
            
            log.info("Created new session: {}", sessionId);
            return Result.ok(response);
            
        } catch (Exception e) {
            log.error("Create session failed: {}", e.getMessage(), e);
            return Result.fail("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", java.time.LocalDateTime.now().toString());
        data.put("version", "1.0.0");
        data.put("database", "CONFIGURED");
        data.put("deepseek", "CONFIGURED");
        return Result.ok(data);
    }
}