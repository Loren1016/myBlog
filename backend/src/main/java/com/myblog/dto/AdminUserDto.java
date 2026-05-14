package com.myblog.dto;

import com.myblog.entity.User;

public class AdminUserDto {

    private String id;
    private String email;
    private String displayName;
    private String avatar;
    private String role;

    public AdminUserDto() {}

    public static AdminUserDto from(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.displayName = user.getDisplayName();
        dto.avatar = user.getAvatar();
        dto.role = user.getRole();
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
