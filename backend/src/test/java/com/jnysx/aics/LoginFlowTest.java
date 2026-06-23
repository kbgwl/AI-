package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录流程测试
 * 验证：未登录用户访问后台应被重定向到登录页
 */
class LoginFlowTest extends BaseIntegrationTest {

    @Test
    void unauthenticatedUserCannotAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithValidCredentials() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/admin/login")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"admin\", \"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void loginThenAccessDashboard() throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        // 先登录
        mockMvc.perform(post("/api/admin/login")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"admin\", \"password\":\"admin123\"}"))
                .andExpect(status().isOk());
        
        // 再访问dashboard
        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void logoutClearsSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        // 先登录
        mockMvc.perform(post("/api/admin/login")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"admin\", \"password\":\"admin123\"}"))
                .andExpect(status().isOk());
        
        // 退出
        mockMvc.perform(post("/api/admin/logout").session(session))
                .andExpect(status().isOk());
        
        // 退出后访问dashboard应返回401
        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isUnauthorized());
    }
}