package com.myblog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.dto.AboutProfileDto;
import com.myblog.entity.AboutProfile;
import com.myblog.repository.AboutProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AboutProfileService {

    private static final String DEFAULT_ID = "default";

    private final AboutProfileRepository repository;
    private final ObjectMapper objectMapper;

    public AboutProfileService(AboutProfileRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public AboutProfileDto getPublicProfile() {
        AboutProfile profile = getOrCreateDefault();
        return toDto(profile);
    }

    public AboutProfileDto getAdminProfile() {
        AboutProfile profile = getOrCreateDefault();
        return toDto(profile);
    }

    public AboutProfileDto updateProfile(AboutProfileDto dto) {
        AboutProfile profile = getOrCreateDefault();
        applyNonNull(dto, profile);
        profile = repository.save(profile);
        return toDto(profile);
    }

    /**
     * 确保默认记录存在（启动初始化使用）。内部已处理并发竞态。
     */
    public void ensureDefaultProfile() {
        getOrCreateDefault();
    }

    private AboutProfile getOrCreateDefault() {
        Optional<AboutProfile> existing = repository.findById(DEFAULT_ID);
        if (existing.isPresent()) {
            return existing.get();
        }
        AboutProfile profile = buildDefaultProfile();
        try {
            return repository.save(profile);
        } catch (DataIntegrityViolationException e) {
            // 并发竞争：另一个请求已插入，重新读取
            return repository.findById(DEFAULT_ID)
                    .orElseThrow(() -> new RuntimeException("无法初始化关于页资料", e));
        }
    }

    private AboutProfile buildDefaultProfile() {
        AboutProfile p = new AboutProfile();
        p.setId(DEFAULT_ID);
        p.setHeroEyebrow("关于");
        p.setHeroTitle("你好。");
        p.setIntroLabel("我是谁");
        p.setBio("我是一名在意工艺、清晰度和细节的软件开发者。我的实践横跨工程严谨与设计感知的交叉地带。\n\n"
                + "这个空间是我记录作品、分享学习、以及探索我对软件开发思考的地方——将软件视为一门需要持续打磨的手艺，而不仅仅是一份工作。\n\n"
                + "我相信好的软件就像好的文章，是反复修订、自我约束和对那些大多数人不会注意但每个人都能感受到的细节的关注所带来的结果。");
        p.setExperienceTitle("经历");
        p.setExperiences("[{\"period\":\"2023 — 至今\",\"role\":\"高级软件工程师\",\"org\":\"独立开发者\"},"
                + "{\"period\":\"2020 — 2023\",\"role\":\"全栈开发工程师\",\"org\":\"某科技工作室\"},"
                + "{\"period\":\"2018 — 2020\",\"role\":\"后端工程师\",\"org\":\"某平台公司\"}]");
        p.setSkillsTitle("技能 & 工具");
        p.setSkills("[\"TypeScript\",\"React\",\"Next.js\",\"Java\",\"Spring Boot\","
                + "\"Python\",\"PostgreSQL\",\"MySQL\",\"Docker\",\"Figma\"]");
        p.setAvatarImage(null);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    }

    private void applyNonNull(AboutProfileDto dto, AboutProfile profile) {
        if (dto.getHeroEyebrow() != null) profile.setHeroEyebrow(dto.getHeroEyebrow());
        if (dto.getHeroTitle() != null) profile.setHeroTitle(dto.getHeroTitle());
        if (dto.getIntroLabel() != null) profile.setIntroLabel(dto.getIntroLabel());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getExperienceTitle() != null) profile.setExperienceTitle(dto.getExperienceTitle());
        if (dto.getExperiences() != null) {
            validateJsonArray(dto.getExperiences(), "experiences");
            profile.setExperiences(dto.getExperiences());
        }
        if (dto.getSkillsTitle() != null) profile.setSkillsTitle(dto.getSkillsTitle());
        if (dto.getSkills() != null) {
            validateJsonArray(dto.getSkills(), "skills");
            profile.setSkills(dto.getSkills());
        }
        if (dto.getAvatarImage() != null) profile.setAvatarImage(dto.getAvatarImage());
    }

    private void validateJsonArray(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                throw new IllegalArgumentException(fieldName + " 必须是 JSON 数组");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException(fieldName + " 不是合法的 JSON: " + e.getMessage());
        }
    }

    private AboutProfileDto toDto(AboutProfile profile) {
        AboutProfileDto dto = new AboutProfileDto();
        dto.setHeroEyebrow(profile.getHeroEyebrow());
        dto.setHeroTitle(profile.getHeroTitle());
        dto.setIntroLabel(profile.getIntroLabel());
        dto.setBio(profile.getBio());
        dto.setExperienceTitle(profile.getExperienceTitle());
        dto.setExperiences(profile.getExperiences());
        dto.setSkillsTitle(profile.getSkillsTitle());
        dto.setSkills(profile.getSkills());
        dto.setAvatarImage(profile.getAvatarImage());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
