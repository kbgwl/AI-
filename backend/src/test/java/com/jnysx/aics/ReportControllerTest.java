package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerTest extends BaseIntegrationTest {

    @Test
    void getDailyReport() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/report/daily")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getDailyReportWithDateRange() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/report/daily")
                .session(session)
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getDailyReportByChannel() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/report/daily")
                .session(session)
                .param("channel", "web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void reportEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/report/daily"))
                .andExpect(status().isUnauthorized());
    }
}