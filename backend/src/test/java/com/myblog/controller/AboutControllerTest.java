package com.myblog.controller;

import com.myblog.dto.AboutProfileDto;
import com.myblog.security.JwtAuthenticationFilter;
import com.myblog.security.JwtProvider;
import com.myblog.service.AboutProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AboutController.class)
@AutoConfigureMockMvc(addFilters = false)
class AboutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AboutProfileService aboutProfileService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void getProfile_shouldReturn200_withProfileData() throws Exception {
        AboutProfileDto mockProfile = new AboutProfileDto();
        mockProfile.setHeroEyebrow("关于");
        mockProfile.setHeroTitle("你好。");
        mockProfile.setIntroLabel("我是谁");
        mockProfile.setBio("测试简介内容");
        mockProfile.setExperienceTitle("经历");
        mockProfile.setExperiences("[{\"period\":\"2023\",\"role\":\"工程师\",\"org\":\"测试\"}]");
        mockProfile.setSkillsTitle("技能 & 工具");
        mockProfile.setSkills("[\"Java\",\"React\"]");

        when(aboutProfileService.getPublicProfile()).thenReturn(mockProfile);

        mockMvc.perform(get("/api/about/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.heroEyebrow").value("关于"))
                .andExpect(jsonPath("$.data.heroTitle").value("你好。"))
                .andExpect(jsonPath("$.data.bio").value("测试简介内容"))
                .andExpect(jsonPath("$.data.skills").value("[\"Java\",\"React\"]"));
    }
}
