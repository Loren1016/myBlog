package com.myblog.service;

import com.myblog.entity.Media;
import com.myblog.repository.MediaRepository;
import org.springframework.stereotype.Service;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Media save(Media media) {
        return mediaRepository.save(media);
    }
}
