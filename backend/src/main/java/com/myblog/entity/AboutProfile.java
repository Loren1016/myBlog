package com.myblog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "about_profile")
public class AboutProfile {

    @Id
    private String id;

    private String heroEyebrow;

    private String heroTitle;

    private String introLabel;

    @Column(columnDefinition = "LONGTEXT")
    private String bio;

    private String experienceTitle;

    @Column(columnDefinition = "JSON")
    private String experiences;

    private String skillsTitle;

    @Column(columnDefinition = "JSON")
    private String skills;

    private String avatarImage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public AboutProfile() {}

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
