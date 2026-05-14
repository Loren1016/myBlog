package com.myblog.service;

import com.myblog.dto.MediaUploadResponse;
import com.myblog.entity.Media;
import com.myblog.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final MediaRepository mediaRepository;
    private final Path uploadDir;

    @Autowired
    public MediaService(MediaRepository mediaRepository,
                        @Value("${file.upload-dir:./uploads}") String uploadDirPath) {
        this.mediaRepository = mediaRepository;
        this.uploadDir = Paths.get(uploadDirPath);
    }

    public MediaUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("仅允许上传图片文件 (jpeg, png, gif, webp)");
        }

        String safeExtension = MIME_TO_EXTENSION.get(mimeType);
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + safeExtension;

        try {
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(storedFilename);
            file.transferTo(targetPath.toFile());

            String mediaId = storedFilename.substring(0, storedFilename.lastIndexOf("."));

            Media media = new Media();
            media.setId(mediaId);
            media.setFilename(originalFilename != null ? originalFilename : storedFilename);
            media.setUrl("/uploads/" + storedFilename);
            media.setSize(file.getSize());
            media.setMimeType(mimeType);

            Media saved = mediaRepository.save(media);
            return MediaUploadResponse.from(saved);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        } catch (RuntimeException e) {
            // DB save failed — clean up the written file
            Path targetPath = uploadDir.resolve(storedFilename);
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException deleteEx) {
                e.addSuppressed(deleteEx);
            }
            throw e;
        }
    }

    public Media save(Media media) {
        return mediaRepository.save(media);
    }
}
