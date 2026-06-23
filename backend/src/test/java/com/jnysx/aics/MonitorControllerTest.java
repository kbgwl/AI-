package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MonitorControllerTest extends BaseIntegrationTest {

    @Test
    void monitorRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/monitor/performance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPerformanceMetrics() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/monitor/performance").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void getHealthStatus() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/monitor/health").session(session))
                .andExpect(status().isOk());
    }
}