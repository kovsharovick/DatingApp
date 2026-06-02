package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserVideo;
import org.example.repository.UserVideoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoCleanupService {
    private final UserVideoRepository videoRepository;
    private final MinioService minioService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanOldInactiveVideos() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<UserVideo> oldVideos = videoRepository.findByActiveFalseAndCreatedAtBefore(threshold);
        for (UserVideo video : oldVideos) {
            try {
                if (video.getVideoUrl() != null) minioService.deleteFile(video.getVideoUrl());
                if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isBlank()) 
                    minioService.deleteFile(video.getThumbnailUrl());
                videoRepository.delete(video);
            } catch (Exception e) {
                log.error("Failed to clean video {}", video.getId(), e);
            }
        }
        log.info("Cleaned {} old inactive videos", oldVideos.size());
    }
}