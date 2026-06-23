package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatControllerTest extends BaseIntegrationTest {

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void aiChatWithEmptySessionId() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"\", \"message\":\"你好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void aiChatWithEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test-001\", \"message\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void aiChatWithNullMessage() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void aiChatWithLongMessage() throws Exception {
        String longMessage = "a".repeat(1001);
        String json = "{\"sessionId\":\"test-001\", \"message\":\"" + longMessage + "\"}";
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void aiChatWithInvalidSessionIdFormat() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test@#$\", \"message\":\"你好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void aiChatWithSqlInjection() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test-001\", \"message\":\"' or '1'='1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void aiChatWithXssInput() throws Exception {
        mockMvc.perform(post("/api/chat/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test-001\", \"message\":\"<script>alert(1)</script>\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void intentRecognition() throws Exception {
        mockMvc.perform(post("/api/chat/intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"我要退货\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.intentCode").value("refund"));
    }

    @Test
    void intentRecognitionEmpty() throws Exception {
        mockMvc.perform(post("/api/chat/intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void toggleRobotMode() throws Exception {
        mockMvc.perform(post("/api/chat/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":\"test-001\", \"useRobot\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.useRobot").value(true));
    }

    @Test
    void chatEndpointDoesNotRequireLogin() throws Exception {
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk());
    }
}