package com.jnysx.aics;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketControllerTest extends BaseIntegrationTest {

    private static Long createdTicketId;

    @Test
    @Order(1)
    void createTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        MvcResult result = mockMvc.perform(post("/api/admin/tickets")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"测试工单_" + System.currentTimeMillis() + "\",\"description\":\"测试描述\",\"ticketType\":\"complaint\",\"priority\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        if (json.contains("\"id\":")) {
            createdTicketId = Long.parseLong(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
        }
    }

    @Test
    @Order(2)
    void listTickets() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/admin/tickets")
                .session(session)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(3)
    void getTicketDetail() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(get("/api/admin/tickets/" + createdTicketId)
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(4)
    void updateTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(put("/api/admin/tickets/" + createdTicketId)
                    .session(session)
                    .contentType("application/json")
                    .content("{\"title\":\"更新后的工单\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(5)
    void assignTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(put("/api/admin/tickets/" + createdTicketId + "/assign")
                    .session(session)
                    .contentType("application/json")
                    .content("{\"agentId\":\"1\",\"agentName\":\"测试坐席\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(6)
    void resolveTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(put("/api/admin/tickets/" + createdTicketId + "/resolve")
                    .session(session)
                    .contentType("application/json")
                    .content("{\"solution\":\"问题已解决\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(7)
    void closeTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(put("/api/admin/tickets/" + createdTicketId + "/close")
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(8)
    void deleteTicket() throws Exception {
        MockHttpSession session = loginAsAdmin();
        if (createdTicketId != null) {
            mockMvc.perform(delete("/api/admin/tickets/" + createdTicketId)
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    void ticketEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/admin/tickets"))
                .andExpect(status().isUnauthorized());
    }
}