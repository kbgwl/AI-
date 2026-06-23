package com.jnysx.aics;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChannelControllerTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void listChannels() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/channels").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    void createChannel() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(post("/api/admin/channel")
                .session(session)
                .contentType("application/json")
                .content("{\"channel\":\"test_" + System.currentTimeMillis() + "\",\"configName\":\"测试渠道\",\"configData\":\"{}\",\"status\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void updateChannel() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(put("/api/admin/channel")
                .session(session)
                .contentType("application/json")
                .content("{\"id\":1,\"name\":\"更新后的渠道\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void channelEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/channels"))
                .andExpect(status().isUnauthorized());
    }
}