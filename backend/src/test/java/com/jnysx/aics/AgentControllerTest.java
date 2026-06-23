package com.jnysx.aics;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentControllerTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void listAgents() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/agents")
                .session(session)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(2)
    void listAgentsWithFilter() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/agents")
                .session(session)
                .param("skillGroup", "after_sale")
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(3)
    void createAgent() throws Exception {
        MockHttpSession session = loginAsAdmin();
        String uniqueName = "agent_" + System.currentTimeMillis();
        mockMvc.perform(post("/api/admin/agent")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"" + uniqueName + "\",\"password\":\"test123\",\"nickname\":\"测试新坐席\",\"role\":\"agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    void updateAgent() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(put("/api/admin/agent")
                .session(session)
                .contentType("application/json")
                .content("{\"id\":1,\"nickname\":\"更新后的坐席名\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    void updateAgentStatus() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(put("/api/admin/agent/1/status")
                .session(session)
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    void getAgentSessions() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/agent/1/sessions")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void agentEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/agents"))
                .andExpect(status().isUnauthorized());
    }
}