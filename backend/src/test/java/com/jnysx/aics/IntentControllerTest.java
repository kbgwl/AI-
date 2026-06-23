package com.jnysx.aics;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntentControllerTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void listIntents() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/intents")
                .session(session)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    void listIntentsByCategory() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/intents")
                .session(session)
                .param("category", "after_sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(3)
    void createIntent() throws Exception {
        MockHttpSession session = loginAsAdmin();
        String uniqueCode = "intent_" + System.currentTimeMillis();
        mockMvc.perform(post("/api/admin/intent")
                .session(session)
                .contentType("application/json")
                .content("{\"intentCode\":\"" + uniqueCode + "\",\"intentName\":\"测试意图\",\"category\":\"general\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    void updateIntent() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(put("/api/admin/intent")
                .session(session)
                .contentType("application/json")
                .content("{\"id\":1,\"intentName\":\"更新后的意图\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    void deleteIntent() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(delete("/api/admin/intent/99999")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void intentEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/intents"))
                .andExpect(status().isUnauthorized());
    }
}