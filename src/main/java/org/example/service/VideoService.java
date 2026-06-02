package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserData;
import org.example.entities.UserVideo;
import org.example.model.VideoResponse;
import org.example.repository.UserDataRepository;
import org.example.repository.UserVideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final MinioService minioService;
    private final UserVideoRepository videoRepository;
    private final UserDataRepository userRepository;
    private final ThumbnailService thumbnailService;
    private final VideoMetadataService videoMetadataService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",
            "video/x-msvideo"
    );

    @Value("${app.video.max-size-mb:250}")
    private long maxSizeMb;

    @Value("${app.video.max-duration-sec:120}")
    private int maxDurationSec;

    @Transactional
    public Long uploadVideo(MultipartFile file, Long userId) {
        validateFile(file);

        UserData user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Path tempVideo = null;
        try {
            tempVideo = Files.createTempFile("upload_", ".mp4");
            file.transferTo(tempVideo.toFile());

            int realDuration = videoMetadataService.getDurationSec(tempVideo);
            if (realDuration > maxDurationSec) {
                throw new IllegalArgumentException(
                        "Video is too long: " + realDuration + "s. Maximum allowed: " + maxDurationSec + "s.");
            }

            String objectName = "videos/user" + userId + "/" + UUID.randomUUID() + ".mp4";

            try (InputStream is = Files.newInputStream(tempVideo)) {
                minioService.uploadFile(objectName, is, Files.size(tempVideo), "video/mp4");
            }

            UserVideo oldActive = user.getActiveVideo();
            if (oldActive != null) {
                oldActive.setActive(false);
                videoRepository.save(oldActive);
            }

            UserVideo video = UserVideo.builder()
                    .user(user)
                    .videoUrl(objectName)
                    .thumbnailUrl("")
                    .durationSec(realDuration)
                    .active(true)
                    .build();
            video = videoRepository.save(video);
            user.setActiveVideo(video);
            userRepository.save(user);

            final UserVideo finalVideo = video;
            thumbnailService.generateAndUploadAsync(objectName, userId)
                    .thenAccept(thumbnailUrl -> {
                        finalVideo.setThumbnailUrl(thumbnailUrl != null ? thumbnailUrl : "");
                        videoRepository.save(finalVideo);
                    });

            return video.getId();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process uploaded video", e);
        } finally {
            if (tempVideo != null) {
                try {
                    Files.deleteIfExists(tempVideo);
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> getMyVideos(Long userId) {
        return videoRepository.findByUserId(userId)
                .stream()
                .map(v -> toResponse(v, userId))
                .collect(Collectors.toList());
    }

    private VideoResponse toResponse(UserVideo v, Long userId) {
        String videoUrl = null;
        String thumbnailUrl = null;
        try {
            videoUrl = minioService.getPresignedUrl(v.getVideoUrl());
        } catch (Exception e) {
            log.warn("Failed to get presigned URL for video {} of user {}", v.getId(), userId, e);
        }
        try {
            if (v.getThumbnailUrl() != null && !v.getThumbnailUrl().isBlank()) {
                thumbnailUrl = minioService.getPresignedUrl(v.getThumbnailUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to get presigned URL for thumbnail of video {}", v.getId(), e);
        }
        return VideoResponse.builder()
                .id(v.getId())
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .durationSec(v.getDurationSec())
                .active(v.isActive())
                .viewsCount(v.getViewsCount())
                .likesCount(v.getLikesCount())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported video format. Allowed: MP4, MOV, AVI.");
        }
        long maxBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "Video file size must not exceed " + maxSizeMb + " MB.");
        }
    }
}
