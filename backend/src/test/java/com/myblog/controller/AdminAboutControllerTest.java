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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAboutController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAboutControllerTest {

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
        mockProfile.setHeroTitle("管理员视图");
        mockProfile.setBio("管理端简介");

        when(aboutProfileService.getAdminProfile()).thenReturn(mockProfile);

        mockMvc.perform(get("/api/admin/about/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.heroTitle").value("管理员视图"));
    }

    @Test
    void updateProfile_shouldReturn200_withUpdatedProfile() throws Exception {
        AboutProfileDto updatedProfile = new AboutProfileDto();
        updatedProfile.setHeroEyebrow("关于");
        updatedProfile.setHeroTitle("更新后的标题");
        updatedProfile.setBio("更新后的简介");
        updatedProfile.setSkills("[\"Go\",\"Rust\"]");

        when(aboutProfileService.updateProfile(any(AboutProfileDto.class))).thenReturn(updatedProfile);

        String requestBody = """
                {
                    "heroTitle": "更新后的标题",
                    "bio": "更新后的简介",
                    "skills": "[\\"Go\\",\\"Rust\\"]"
                }
                """;

        mockMvc.perform(put("/api/admin/about/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.heroTitle").value("更新后的标题"))
                .andExpect(jsonPath("$.data.bio").value("更新后的简介"))
                .andExpect(jsonPath("$.data.skills").value("[\"Go\",\"Rust\"]"));
    }
}
