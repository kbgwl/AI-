package com.jnysx.aics;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SmartRoutingControllerTest extends BaseIntegrationTest {

    @Test
    void getSkillGroupsRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/routing/skill-groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSkillGroupsWithLogin() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/routing/skill-groups").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void getAgentSkills() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/routing/agent/1/skills").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void testRouteWithLogin() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mockMvc.perform(get("/api/routing/test-route")
                .session(session)
                .param("intentCode", "refund"))
                .andExpect(status().isOk());
    }
}