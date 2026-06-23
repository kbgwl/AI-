package com.jnysx.aics.service;

import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.entity.KbItem;
import com.jnysx.aics.entity.Message;
import com.jnysx.aics.entity.Session;
import com.jnysx.aics.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 对话编排器
 * 吸收 AI 对话的 10 步编排逻辑：验证 → 意图 → 转人工 → RAG → AI → 持久化
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private final DeepSeekService deepSeekService;
    private final MessageService messageService;
    private final IntentRecognitionService intentRecognitionService;
    private final TicketService ticketService;
    private final RagService ragService;
    private final KbItemService kbItemService;
    private final InputSanitizer inputSanitizer;
    private final SessionService sessionService;
    private final AgentService agentService;

    public ChatOrchestrator(
            DeepSeekService deepSeekService,
            MessageService messageService,
            IntentRecognitionService intentRecognitionService,
            TicketService ticketService,
            RagService ragService,
            KbItemService kbItemService,
            InputSanitizer inputSanitizer,
            SessionService sessionService,
            AgentService agentService) {
        this.deepSeekService = deepSeekService;
        this.messageService = messageService;
        this.intentRecognitionService = intentRecognitionService;
        this.ticketService = ticketService;
        this.ragService = ragService;
        this.kbItemService = kbItemService;
        this.inputSanitizer = inputSanitizer;
        this.sessionService = sessionService;
        this.agentService = agentService;
    }

    /**
     * 处理用户消息并返回 AI 回复
     */
    public Map<String, Object> processMessage(String sessionId, String userMessage, Boolean useRobot) {
        // 保存用户消息
        Message userMsg = new Message();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(userMessage);
        userMsg.setSenderType("user");
        messageService.save(userMsg);

        // 如果未启用机器人，直接返回提示消息
        if (useRobot == null || !useRobot) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "您的消息已发送，客服正在处理中...");
            response.put("isRobot", false);
            return response;
        }

        // 获取会话上下文（最近 10 条对话）
        List<Message> history = messageService.getRecentMessages(sessionId, 10);
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        for (Message msg : history) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", "user".equals(msg.getSenderType()) ? "user" : "assistant");
            msgMap.put("content", msg.getContent());
            conversationHistory.add(msgMap);
        }

        // 意图识别
        IntentRecognitionService.IntentResult intentResult = intentRecognitionService.recognize(userMessage);
        log.info("Intent recognized for session {}: {} (confidence: {}, type: {})",
                 sessionId, intentResult.getIntentCode(), intentResult.getConfidence(), intentResult.getMatchType());

        // 检测是否需要转人工
        boolean needTransfer = false;
        String transferReason = "";
        boolean shouldCreateTicket = false;
        String ticketType = "";

        if ("transfer_manual".equals(intentResult.getIntentCode()) && intentResult.getConfidence() >= 0.6) {
            needTransfer = true;
            transferReason = "用户明确要求转人工";
        } else if ("complaint".equals(intentResult.getIntentCode()) && intentResult.getConfidence() >= 0.7) {
            needTransfer = true;
            transferReason = "检测到投诉内容";
            shouldCreateTicket = true;
            ticketType = "complaint";
        } else if (intentResult.getConfidence() < 0.3) {
            needTransfer = true;
            transferReason = "问题复杂或无法识别";
            shouldCreateTicket = true;
            ticketType = "complex";
        }

        // 如果需要转人工，创建工单并返回提示
        if (needTransfer) {
            Ticket createdTicket = null;
            if (shouldCreateTicket) {
                createdTicket = ticketService.autoCreateTicket(
                    sessionId, sessionId, ticketType,
                    "客服转接：" + transferReason,
                    userMessage + " (置信度：" + String.format("%.2f", intentResult.getConfidence()) + ")",
                    "complaint".equals(intentResult.getIntentCode()) ? 3 : 2,
                    intentResult.getIntentCode()
                );
                log.info("Auto created ticket {} for session {}", createdTicket.getTicketNo(), sessionId);
            }

            String transferMsg = "检测到您可能需要人工客服支持（" + transferReason + "），正在为您转接...";
            if (shouldCreateTicket && createdTicket != null) {
                transferMsg += "我们已为您创建工单（单号：" + createdTicket.getTicketNo() + "），坐席将尽快处理。";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", transferMsg);
            response.put("isRobot", true);
            response.put("needTransfer", true);
            response.put("intent", intentResult.getIntentCode());
            response.put("confidence", intentResult.getConfidence());
            return response;
        }

        // 系统提示词
        String systemPrompt = "你是一名专业的 AI 客服助手，服务于空白格·AI 公司的智能客服系统。" +
                "请友好、专业地回答用户问题。如果问题不清楚，请引导用户提供更多信息。" +
                "如果遇到无法回答的问题，请建议用户转人工客服。" +
                "回答要简洁明了，避免过长。" +
                "当前识别到的用户意图：" + intentResult.getIntentCode() + "（置信度：" + (int)(intentResult.getConfidence() * 100) + "%）";

        // 调用 DeepSeek API
        log.info("Calling DeepSeek API for session: {}, intent: {}", sessionId, intentResult.getIntentCode());
        String aiResponse = deepSeekService.chat(userMessage, systemPrompt, conversationHistory);

        // RAG 知识库增强
        List<KbItem> kbItems = kbItemService.list();
        List<RagService.KbItem> ragItems = new ArrayList<>();
        for (KbItem kb : kbItems) {
            ragItems.add(new RagService.KbItem(kb.getId(), kb.getQuestion(), kb.getAnswer(), String.valueOf(kb.getCategoryId())));
        }
        List<RagService.KbSearchResult> relatedDocs = ragService.search(userMessage, ragItems);

        if (!relatedDocs.isEmpty()) {
            aiResponse = ragService.generateAnswer(userMessage, relatedDocs, aiResponse);
            log.info("RAG enhanced response with {} documents", relatedDocs.size());
        }

        // 保存 AI 回复
        Message aiMsg = new Message();
        aiMsg.setSessionId(sessionId);
        aiMsg.setContent(aiResponse != null ? aiResponse : "抱歉，AI 服务暂时不可用。");
        aiMsg.setSenderType("ai");
        messageService.save(aiMsg);

        // 返回响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", aiResponse != null ? aiResponse : "抱歉，AI 服务暂时不可用。");
        response.put("isRobot", true);
        response.put("messageId", aiMsg.getId());

        log.info("AI chat completed for session {}: {}", sessionId, aiResponse != null ? "success" : "failed");

        return response;
    }

    /**
     * 转接人工客服
     * @param sessionId 会话ID
     * @return 转接结果
     */
    public Map<String, Object> transferToAgent(String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查找可用的客服
        Optional<Agent> availableAgent = agentService.findAvailableAgent();
        
        if (availableAgent.isEmpty()) {
            result.put("success", false);
            result.put("message", "当前没有在线客服，请稍后再试");
            return result;
        }
        
        Agent agent = availableAgent.get();
        
        // 更新会话状态，分配客服
        boolean assigned = sessionService.assignAgent(sessionId, agent.getId());
        if (!assigned) {
            result.put("success", false);
            result.put("message", "会话分配失败");
            return result;
        }
        
        // 增加客服当前会话数
        agentService.incrementCurrentSessions(agent.getId());
        
        // 保存系统消息
        Message systemMsg = new Message();
        systemMsg.setSessionId(sessionId);
        systemMsg.setContent("已为您转接人工客服「" + agent.getNickname() + "」，请稍候...");
        systemMsg.setSenderType("system");
        messageService.save(systemMsg);
        
        result.put("success", true);
        result.put("agentId", agent.getId());
        result.put("agentName", agent.getNickname());
        result.put("message", "已为您转接人工客服「" + agent.getNickname() + "」");
        
        log.info("Session {} transferred to agent {} ({})", sessionId, agent.getId(), agent.getNickname());
        
        return result;
    }

    /**
     * 客服发送消息
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @param content 消息内容
     * @return 发送结果
     */
    public Map<String, Object> agentSendMessage(String sessionId, Long agentId, String content) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取客服信息
        Agent agent = agentService.getById(agentId);
        if (agent == null) {
            result.put("success", false);
            result.put("message", "客服不存在");
            return result;
        }
        
        // 保存客服消息
        Message agentMsg = new Message();
        agentMsg.setSessionId(sessionId);
        agentMsg.setContent(content);
        agentMsg.setSenderType("agent");
        agentMsg.setSenderId(agentId);
        agentMsg.setSenderName(agent.getNickname());
        messageService.save(agentMsg);
        
        result.put("success", true);
        result.put("messageId", agentMsg.getId());
        
        log.info("Agent {} sent message to session {}", agentId, sessionId);
        
        return result;
    }

    /**
     * 获取会话消息列表
     * @param sessionId 会话ID
     * @param limit 数量限制
     * @return 消息列表
     */
    public List<Message> getSessionMessages(String sessionId, int limit) {
        return messageService.getRecentMessages(sessionId, limit);
    }

    public InputSanitizer getInputSanitizer() {
        return inputSanitizer;
    }
}