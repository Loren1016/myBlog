package com.myblog.dto;

import com.myblog.entity.Media;

import java.time.LocalDateTime;

public class MediaUploadResponse {

    private String id;
    private String filename;
    private String url;
    private Long size;
    private String mimeType;
    private LocalDateTime uploadedAt;

    public MediaUploadResponse() {}

    public static MediaUploadResponse from(Media media) {
        MediaUploadResponse dto = new MediaUploadResponse();
        dto.setId(media.getId());
        dto.setFilename(media.getFilename());
        dto.setUrl(media.getUrl());
        dto.setSize(media.getSize());
        dto.setMimeType(media.getMimeType());
        dto.setUploadedAt(media.getUploadedAt());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
