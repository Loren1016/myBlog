package com.myblog.service;

import com.myblog.dto.MediaUploadResponse;
import com.myblog.entity.Media;
import com.myblog.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    private Path uploadDir;
    private MediaService mediaService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.uploadDir = tempDir.resolve("uploads");
        mediaService = new MediaService(mediaRepository, uploadDir.toString());
        lenient().when(mediaRepository.save(any(Media.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void upload_shouldSaveFileToDiskAndReturnDto() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-image.png", "image/png", "fake-image-data".getBytes());

        MediaUploadResponse result = mediaService.upload(file);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test-image.png", result.getFilename());
        assertEquals("image/png", result.getMimeType());
        assertEquals(file.getSize(), result.getSize());
        assertNotNull(result.getUploadedAt());
        assertTrue(result.getUrl().startsWith("/uploads/"));
        assertTrue(result.getUrl().endsWith(".png"));

        Path savedFile = uploadDir.resolve(result.getUrl().replace("/uploads/", ""));
        assertTrue(Files.exists(savedFile));
        assertArrayEquals("fake-image-data".getBytes(), Files.readAllBytes(savedFile));

        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void upload_shouldGenerateUniqueStoredPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "some-image-bytes".getBytes());

        MediaUploadResponse result = mediaService.upload(file);

        assertEquals("photo.jpg", result.getFilename());
        assertNotEquals("/uploads/photo.jpg", result.getUrl());
        assertTrue(result.getUrl().matches("/uploads/[0-9a-f-]+\\.jpg"),
                "URL should use UUID-based name: " + result.getUrl());
    }

    @Test
    void upload_shouldThrowOnEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(file));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("空"));
    }

    @Test
    void upload_shouldThrowOnNullFile() {
        assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(null));
    }

    @Test
    void upload_shouldThrowOnNonImageType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf-content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(file));
        assertTrue(ex.getMessage().contains("image")
                || ex.getMessage().contains("图片")
                || ex.getMessage().contains("类型"));
    }

    @Test
    void upload_shouldRejectTextPlainType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.txt", "text/plain", "malicious".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(file));
    }

    @Test
    void upload_shouldAcceptAllAllowedImageTypes() throws IOException {
        String[][] allowed = {
                {"image/jpeg", "photo.jpg"},
                {"image/png", "photo.png"},
                {"image/gif", "photo.gif"},
                {"image/webp", "photo.webp"},
        };

        for (String[] pair : allowed) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", pair[1], pair[0], "data".getBytes());
            MediaUploadResponse result = mediaService.upload(file);
            assertEquals(pair[0], result.getMimeType());
        }
    }

    // ---- Issue 1: Safe extension from MIME type ----

    @Test
    void upload_shouldUsePngExtension_whenOriginalFileIsHtml() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "payload.html", "image/png", "data".getBytes());

        MediaUploadResponse result = mediaService.upload(file);

        assertTrue(result.getUrl().endsWith(".png"),
                "URL must end with .png from MIME, not .html: " + result.getUrl());
        assertEquals("payload.html", result.getFilename(),
                "Original filename preserved in filename field");
    }

    @Test
    void upload_shouldUseJpgExtension_whenOriginalFileHasNoExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noext", "image/jpeg", "data".getBytes());

        MediaUploadResponse result = mediaService.upload(file);

        assertTrue(result.getUrl().endsWith(".jpg"),
                "URL must end with .jpg from MIME type: " + result.getUrl());
    }

    // ---- Relative path → absolute path ----

    @Test
    void constructor_shouldConvertRelativePathToAbsolute() {
        MediaService service = new MediaService(mediaRepository, "./uploads");

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "data".getBytes());

        MediaUploadResponse result = service.upload(file);

        assertNotNull(result);
        assertTrue(result.getUrl().endsWith(".png"));

        // The file should have been saved to the JVM's CWD-relative absolute path,
        // not to Tomcat's temp directory. Clean up afterwards.
        Path absoluteUploadsDir = Paths.get("./uploads").toAbsolutePath().normalize();
        Path savedFile = absoluteUploadsDir.resolve(result.getUrl().replace("/uploads/", ""));
        assertTrue(Files.exists(savedFile),
                "File should exist at: " + savedFile);
        // Clean up
        try { Files.deleteIfExists(savedFile); } catch (IOException ignored) {}
    }

    // ---- Issue 5: Orphan file cleanup on DB failure ----

    @Test
    void upload_shouldCleanUpFile_whenDbSaveFails() throws IOException {
        reset(mediaRepository);
        when(mediaRepository.save(any(Media.class)))
                .thenThrow(new RuntimeException("DB down"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mediaService.upload(file));
        assertEquals("DB down", ex.getMessage());

        // No orphan file should remain on disk
        try (var entries = Files.list(uploadDir)) {
            assertEquals(0, entries.count(),
                    "Upload directory should be empty after DB failure");
        }
    }
}
