package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 访问控制测试
 * 验证：未登录用户访问受保护资源应返回401
 */
class AccessControlTest extends BaseIntegrationTest {

    @Test
    void dashboardRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void agentsRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/agents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ticketsRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void intentsRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/intents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void kbReadDoesNotRequireLogin() throws Exception {
        mockMvc.perform(get("/api/kb/items"))
                .andExpect(status().isOk());
    }

    @Test
    void chatDoesNotRequireLogin() throws Exception {
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk());
    }
}