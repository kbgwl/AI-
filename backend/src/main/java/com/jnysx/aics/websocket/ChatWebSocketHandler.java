package com.jnysx.aics.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jnysx.aics.entity.Agent;
import com.jnysx.aics.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 聊天WebSocket处理器（状态委托给 SessionManager）
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final AgentService agentService;
    private final SessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = getPathParam(session, "sessionId");
        String agentIdStr = getPathParam(session, "agentId");

        if (agentIdStr != null) {
            Long agentId = Long.parseLong(agentIdStr);
            sessionManager.registerAgentSession(agentId, session);
            Agent agent = new Agent();
            agent.setId(agentId);
            agent.setStatus(1);
            agentService.updateById(agent);
            log.info("[客服上线] agentId={}", agentId);
        } else if (sessionId != null) {
            sessionManager.registerUserSession(sessionId, session);
            log.info("[用户连接] sessionId={}", sessionId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject msg = JSON.parseObject(message.getPayload());
        String type = msg.getString("type");

        switch (type) {
            case "chat" -> handleChatMessage(msg);
            case "typing" -> handleTypingIndicator(msg);
            case "transfer" -> handleTransferRequest(msg);
            case "status" -> handleStatusUpdate(msg);
            case "csat" -> handleCsatSubmit(msg);
            case "close" -> handleCloseSession(msg);
            default -> log.warn("[未知消息类型] type={}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = getPathParam(session, "sessionId");
        String agentIdStr = getPathParam(session, "agentId");

        if (agentIdStr != null) {
            Long agentId = Long.parseLong(agentIdStr);
            sessionManager.removeAgentSession(agentId);
            Agent agent = new Agent();
            agent.setId(agentId);
            agent.setStatus(0);
            agentService.updateById(agent);
            log.info("[客服下线] agentId={}", agentId);
        } else if (sessionId != null) {
            sessionManager.removeUserSession(sessionId);
            log.info("[用户断开] sessionId={}", sessionId);
        }
    }

    private void handleChatMessage(JSONObject msg) throws Exception {
        String sessionId = msg.getString("sessionId");
        String senderType = msg.getString("senderType");
        String content = msg.getString("content");

        if ("user".equals(senderType)) {
            Long agentId = sessionManager.getAgentForSession(sessionId);
            if (agentId != null) {
                JSONObject forward = new JSONObject();
                forward.put("type", "chat");
                forward.put("sessionId", sessionId);
                forward.put("senderType", "user");
                forward.put("content", content);
                forward.put("timestamp", System.currentTimeMillis());
                sessionManager.sendToAgent(agentId, forward);
            }
        } else if ("agent".equals(senderType)) {
            JSONObject forward = new JSONObject();
            forward.put("type", "chat");
            forward.put("senderType", "agent");
            forward.put("senderName", msg.getString("senderName"));
            forward.put("senderAvatar", msg.getString("senderAvatar"));
            forward.put("content", content);
            forward.put("timestamp", System.currentTimeMillis());
            sessionManager.sendToUser(sessionId, forward);
        }
    }

    private void handleTypingIndicator(JSONObject msg) throws Exception {
        String sessionId = msg.getString("sessionId");
        String senderType = msg.getString("senderType");

        JSONObject indicator = new JSONObject();
        indicator.put("type", "typing");
        indicator.put("sessionId", sessionId);
        indicator.put("senderType", senderType);

        if ("user".equals(senderType)) {
            Long agentId = sessionManager.getAgentForSession(sessionId);
            if (agentId != null) {
                sessionManager.sendToAgent(agentId, indicator);
            }
        } else {
            sessionManager.sendToUser(sessionId, indicator);
        }
    }

    private void handleTransferRequest(JSONObject msg) throws Exception {
        String sessionId = msg.getString("sessionId");
        Long agentId = msg.getLong("agentId");

        if (agentId != null) {
            sessionManager.mapSessionToAgent(sessionId, agentId);
            JSONObject notification = new JSONObject();
            notification.put("type", "new_session");
            notification.put("sessionId", sessionId);
            notification.put("userId", msg.getLong("userId"));
            notification.put("timestamp", System.currentTimeMillis());
            sessionManager.sendToAgent(agentId, notification);
        }
    }

    private void handleStatusUpdate(JSONObject msg) throws Exception {
        Long agentId = msg.getLong("agentId");
        Integer status = msg.getInteger("status");

        JSONObject statusMsg = new JSONObject();
        statusMsg.put("type", "agent_status");
        statusMsg.put("agentId", agentId);
        statusMsg.put("status", status);

        sessionManager.broadcastToUsers(statusMsg);
    }

    private void handleCsatSubmit(JSONObject msg) throws Exception {
        String sessionId = msg.getString("sessionId");
        Long agentId = sessionManager.getAgentForSession(sessionId);
        if (agentId != null) {
            JSONObject csatMsg = new JSONObject();
            csatMsg.put("type", "csat");
            csatMsg.put("sessionId", sessionId);
            csatMsg.put("score", msg.getInteger("score"));
            sessionManager.sendToAgent(agentId, csatMsg);
        }
    }

    private void handleCloseSession(JSONObject msg) {
        String sessionId = msg.getString("sessionId");
        sessionManager.removeSessionAgentMapping(sessionId);
        sessionManager.removeUserSession(sessionId);
        log.info("[会话关闭] sessionId={}", sessionId);
    }

    private String getPathParam(WebSocketSession session, String paramName) {
        String query = session.getUri().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return kv[1];
            }
        }
        return null;
    }
}
