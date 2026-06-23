package com.jnysx.aics.websocket;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的会话管理器（生产环境）
 */
@Component
public class InMemorySessionManager implements SessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, WebSocketSession> agentSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> sessionAgentMap = new ConcurrentHashMap<>();

    @Override
    public void registerUserSession(String sessionId, WebSocketSession session) {
        userSessions.put(sessionId, session);
    }

    @Override
    public void registerAgentSession(Long agentId, WebSocketSession session) {
        agentSessions.put(agentId, session);
    }

    @Override
    public void removeUserSession(String sessionId) {
        userSessions.remove(sessionId);
    }

    @Override
    public void removeAgentSession(Long agentId) {
        agentSessions.remove(agentId);
    }

    @Override
    public void mapSessionToAgent(String sessionId, Long agentId) {
        sessionAgentMap.put(sessionId, agentId);
    }

    @Override
    public void removeSessionAgentMapping(String sessionId) {
        sessionAgentMap.remove(sessionId);
    }

    @Override
    public Long getAgentForSession(String sessionId) {
        return sessionAgentMap.get(sessionId);
    }

    @Override
    public void sendToUser(String sessionId, JSONObject msg) throws IOException {
        WebSocketSession ws = userSessions.get(sessionId);
        if (ws != null && ws.isOpen()) {
            ws.sendMessage(new TextMessage(msg.toJSONString()));
        }
    }

    @Override
    public void sendToAgent(Long agentId, JSONObject msg) throws IOException {
        WebSocketSession ws = agentSessions.get(agentId);
        if (ws != null && ws.isOpen()) {
            ws.sendMessage(new TextMessage(msg.toJSONString()));
        }
    }

    @Override
    public void broadcastToUsers(JSONObject msg) throws IOException {
        for (WebSocketSession ws : userSessions.values()) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(msg.toJSONString()));
            }
        }
    }
}
