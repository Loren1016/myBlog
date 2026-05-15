package com.myblog.dto;

import java.time.LocalDateTime;

public class AboutProfileDto {

    private String heroEyebrow;
    private String heroTitle;
    private String introLabel;
    private String bio;
    private String experienceTitle;
    private String experiences;
    private String skillsTitle;
    private String skills;
    private String avatarImage;
    private LocalDateTime updatedAt;

    public String getHeroEyebrow() { return heroEyebrow; }
    public void setHeroEyebrow(String heroEyebrow) { this.heroEyebrow = heroEyebrow; }
    public String getHeroTitle() { return heroTitle; }
    public void setHeroTitle(String heroTitle) { this.heroTitle = heroTitle; }
    public String getIntroLabel() { return introLabel; }
    public void setIntroLabel(String introLabel) { this.introLabel = introLabel; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getExperienceTitle() { return experienceTitle; }
    public void setExperienceTitle(String experienceTitle) { this.experienceTitle = experienceTitle; }
    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
    public String getSkillsTitle() { return skillsTitle; }
    public void setSkillsTitle(String skillsTitle) { this.skillsTitle = skillsTitle; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getAvatarImage() { return avatarImage; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
