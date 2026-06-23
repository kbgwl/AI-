package com.jnysx.aics.websocket;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

/**
 * WebSocket 会话管理器接口
 * 将会话状态从 handler 中抽离，使其可测试
 */
public interface SessionManager {

    void registerUserSession(String sessionId, org.springframework.web.socket.WebSocketSession session);

    void registerAgentSession(Long agentId, org.springframework.web.socket.WebSocketSession session);

    void removeUserSession(String sessionId);

    void removeAgentSession(Long agentId);

    void mapSessionToAgent(String sessionId, Long agentId);

    void removeSessionAgentMapping(String sessionId);

    Long getAgentForSession(String sessionId);

    void sendToUser(String sessionId, JSONObject msg) throws IOException;

    void sendToAgent(Long agentId, JSONObject msg) throws IOException;

    void broadcastToUsers(JSONObject msg) throws IOException;
}
