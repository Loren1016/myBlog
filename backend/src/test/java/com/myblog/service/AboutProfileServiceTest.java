package com.myblog.service;

import com.myblog.dto.AboutProfileDto;
import com.myblog.entity.AboutProfile;
import com.myblog.repository.AboutProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AboutProfileServiceTest {

    @Mock
    private AboutProfileRepository repository;

    private AboutProfileService service;

    @BeforeEach
    void setUp() {
        service = new AboutProfileService(repository);
        lenient().when(repository.save(any(AboutProfile.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void getPublicProfile_shouldReturnDefault_whenNoRecordExists() {
        when(repository.findById("default")).thenReturn(Optional.empty());

        AboutProfileDto result = service.getPublicProfile();

        assertNotNull(result);
        assertEquals("关于", result.getHeroEyebrow());
        assertEquals("你好。", result.getHeroTitle());
        assertEquals("我是谁", result.getIntroLabel());
        assertNotNull(result.getBio());
        assertEquals("经历", result.getExperienceTitle());
        assertNotNull(result.getExperiences());
        assertEquals("技能 & 工具", result.getSkillsTitle());
        assertNotNull(result.getSkills());

        verify(repository).save(any(AboutProfile.class));
    }

    @Test
    void getPublicProfile_shouldReturnExisting_whenRecordExists() {
        AboutProfile existing = new AboutProfile();
        existing.setId("default");
        existing.setHeroEyebrow("关于我");
        existing.setHeroTitle("哈喽。");
        existing.setIntroLabel("简介");
        existing.setBio("这是我的故事。");
        existing.setExperienceTitle("工作经历");
        existing.setExperiences("[{\"period\":\"2021-2023\",\"role\":\"Dev\",\"org\":\"ACME\"}]");
        existing.setSkillsTitle("技能");
        existing.setSkills("[\"Java\"]");
        existing.setAvatarImage(null);

        when(repository.findById("default")).thenReturn(Optional.of(existing));

        AboutProfileDto result = service.getPublicProfile();

        assertEquals("关于我", result.getHeroEyebrow());
        assertEquals("哈喽。", result.getHeroTitle());
        assertEquals("简介", result.getIntroLabel());
        assertEquals("这是我的故事。", result.getBio());
        assertEquals("工作经历", result.getExperienceTitle());
        assertEquals("[\"Java\"]", result.getSkills());

        verify(repository, never()).save(any());
    }

    @Test
    void getAdminProfile_shouldReturnExisting() {
        AboutProfile existing = new AboutProfile();
        existing.setId("default");
        existing.setHeroTitle("Admin Title");
        existing.setBio("Admin bio");

        when(repository.findById("default")).thenReturn(Optional.of(existing));

        AboutProfileDto result = service.getAdminProfile();

        assertEquals("Admin Title", result.getHeroTitle());
        assertEquals("Admin bio", result.getBio());
    }

    @Test
    void updateProfile_shouldUpdateAllNonNullFields() {
        AboutProfile existing = new AboutProfile();
        existing.setId("default");
        existing.setHeroEyebrow("old eyebrow");
        existing.setHeroTitle("old title");
        existing.setIntroLabel("old label");
        existing.setBio("old bio");
        existing.setExperienceTitle("old exp title");
        existing.setExperiences("[]");
        existing.setSkillsTitle("old skills title");
        existing.setSkills("[]");
        existing.setAvatarImage("old-image.jpg");

        when(repository.findById("default")).thenReturn(Optional.of(existing));

        AboutProfileDto dto = new AboutProfileDto();
        dto.setHeroTitle("new title");
        dto.setBio("new bio");
        dto.setSkills("[\"Python\"]");

        AboutProfileDto result = service.updateProfile(dto);

        assertEquals("new title", result.getHeroTitle());
        assertEquals("new bio", result.getBio());
        assertEquals("[\"Python\"]", result.getSkills());

        verify(repository).save(existing);
    }

    @Test
    void updateProfile_shouldKeepOldValue_whenFieldIsNull() {
        AboutProfile existing = new AboutProfile();
        existing.setId("default");
        existing.setHeroEyebrow("old eyebrow");
        existing.setHeroTitle("old title");
        existing.setBio("old bio");

        when(repository.findById("default")).thenReturn(Optional.of(existing));

        AboutProfileDto dto = new AboutProfileDto();
        dto.setHeroTitle("new title");

        AboutProfileDto result = service.updateProfile(dto);

        assertEquals("new title", result.getHeroTitle());
        assertEquals("old eyebrow", result.getHeroEyebrow());
        assertEquals("old bio", result.getBio());
    }

    @Test
    void updateProfile_shouldUpdateWithEmptyList() {
        AboutProfile existing = new AboutProfile();
        existing.setId("default");
        existing.setSkills("[\"Java\",\"Python\"]");
        existing.setExperiences("[{\"period\":\"2020\",\"role\":\"Dev\",\"org\":\"Co\"}]");

        when(repository.findById("default")).thenReturn(Optional.of(existing));

        AboutProfileDto dto = new AboutProfileDto();
        dto.setSkills("[]");
        dto.setExperiences("[]");

        AboutProfileDto result = service.updateProfile(dto);

        assertEquals("[]", result.getSkills());
        assertEquals("[]", result.getExperiences());
    }

    @Test
    void getPublicProfile_shouldSaveDefaultRecord_andReturnIt() {
        when(repository.findById("default")).thenReturn(Optional.empty());

        ArgumentCaptor<AboutProfile> captor = ArgumentCaptor.forClass(AboutProfile.class);
        AboutProfileDto result = service.getPublicProfile();

        verify(repository).save(captor.capture());
        AboutProfile saved = captor.getValue();

        assertEquals("default", saved.getId());
        assertEquals("关于", saved.getHeroEyebrow());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }
}
