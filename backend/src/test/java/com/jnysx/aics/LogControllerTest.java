package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LogControllerTest extends BaseIntegrationTest {

    @Test
    void listLogs() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/logs")
                .session(session)
                .param("page", "1")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listLogsByModule() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/logs")
                .session(session)
                .param("module", "auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void logEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isUnauthorized());
    }
}