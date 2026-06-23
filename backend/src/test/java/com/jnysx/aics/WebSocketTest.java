package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSocketTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void webSocketEndpointExists() throws Exception {
        mockMvc.perform(get("/ws/chat"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webSocketAgentEndpointExists() throws Exception {
        mockMvc.perform(get("/ws/agent"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webSocketWithSessionId() throws Exception {
        mockMvc.perform(get("/ws/chat").param("sessionId", "test-session"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webSocketWithAgentId() throws Exception {
        mockMvc.perform(get("/ws/agent").param("agentId", "1"))
                .andExpect(status().isBadRequest());
    }
}