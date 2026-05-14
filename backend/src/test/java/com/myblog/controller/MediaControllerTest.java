package com.myblog.controller;

import com.myblog.dto.MediaUploadResponse;
import com.myblog.security.JwtAuthenticationFilter;
import com.myblog.security.JwtProvider;
import com.myblog.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProvider jwtProvider;

    private MediaUploadResponse createMockResponse(String filename, String url, String mimeType) {
        MediaUploadResponse response = new MediaUploadResponse();
        response.setId("test-uuid-12345");
        response.setFilename(filename);
        response.setUrl(url);
        response.setSize(1024L);
        response.setMimeType(mimeType);
        response.setUploadedAt(LocalDateTime.now());
        return response;
    }

    @Test
    void upload_shouldReturn200_withCorrectStructure() throws Exception {
        MediaUploadResponse mockResp = createMockResponse(
                "abc123.png", "/uploads/abc123.png", "image/png");

        when(mediaService.upload(any())).thenReturn(mockResp);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/admin/media/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("test-uuid-12345"))
                .andExpect(jsonPath("$.data.filename").value("abc123.png"))
                .andExpect(jsonPath("$.data.url").value("/uploads/abc123.png"))
                .andExpect(jsonPath("$.data.size").value(1024))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"))
                .andExpect(jsonPath("$.data.uploadedAt").isNotEmpty());
    }

    @Test
    void upload_shouldReturn400_whenEmptyFile() throws Exception {
        when(mediaService.upload(any()))
                .thenThrow(new IllegalArgumentException("文件不能为空"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/admin/media/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void upload_shouldReturn400_whenNonImageType() throws Exception {
        when(mediaService.upload(any()))
                .thenThrow(new IllegalArgumentException("仅允许上传图片文件"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/api/admin/media/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void upload_shouldAcceptWebpImage() throws Exception {
        MediaUploadResponse mockResp = createMockResponse(
                "def456.webp", "/uploads/def456.webp", "image/webp");

        when(mediaService.upload(any())).thenReturn(mockResp);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.webp", "image/webp", "webpdata".getBytes());

        mockMvc.perform(multipart("/api/admin/media/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mimeType").value("image/webp"));
    }
}
