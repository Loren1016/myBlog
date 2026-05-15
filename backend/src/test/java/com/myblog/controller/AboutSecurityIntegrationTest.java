package com.myblog.controller;

import com.myblog.dto.AboutProfileDto;
import com.myblog.service.AboutProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 验证 SecurityConfig 对 /api/about/** 和 /api/admin/about/** 的认证规则。
 * 使用真实 SecurityFilterChain（不含 addFilters = false）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AboutSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AboutProfileService aboutProfileService;

    @Test
    void publicEndpoint_shouldReturn200_withoutToken() throws Exception {
        AboutProfileDto mockProfile = new AboutProfileDto();
        mockProfile.setHeroTitle("Public Profile");
        mockProfile.setBio("No auth needed");
        when(aboutProfileService.getPublicProfile()).thenReturn(mockProfile);

        mockMvc.perform(get("/api/about/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.heroTitle").value("Public Profile"));
    }

    @Test
    void adminGetEndpoint_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/about/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
