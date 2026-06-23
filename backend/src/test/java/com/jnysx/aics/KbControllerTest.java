package com.jnysx.aics;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KbControllerTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void listIndustries() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/kb/industries").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void listCategories() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/kb/categories").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void createCategory() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(post("/api/kb/category")
                .session(session)
                .contentType("application/json")
                .content("{\"name\":\"测试分类_" + System.currentTimeMillis() + "\",\"parentId\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void listKBItems() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/kb/items")
                .session(session)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void createKBItem() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(post("/api/kb/item")
                .session(session)
                .contentType("application/json")
                .content("{\"question\":\"测试问题_" + System.currentTimeMillis() + "\",\"answer\":\"测试答案\",\"categoryId\":1,\"itemType\":\"faq\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void listUnknownQuestions() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/kb/unknown")
                .session(session)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void kbEndpointRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/kb/industries"))
                .andExpect(status().isUnauthorized());
    }
}