package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserData;
import org.example.entities.UserVideo;
import org.example.repository.UserDataRepository;
import org.example.repository.UserVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final MinioService minioService;
    private final UserVideoRepository videoRepository;
    private final UserDataRepository userRepository;
    private final ThumbnailService thumbnailService;
    private final VideoMetadataService videoMetadataService;

    @Transactional
    public Long uploadVideo(MultipartFile file, Long userId) {
        UserData user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Path tempVideo = null;
        try {
            tempVideo = Files.createTempFile("upload_", ".mp4");
            file.transferTo(tempVideo.toFile());

            int realDuration = videoMetadataService.getDurationSec(tempVideo);

            String objectName = "videos/user" + userId + "/" + UUID.randomUUID() + ".mp4";

            try (InputStream is = Files.newInputStream(tempVideo)) {
                minioService.uploadFile(objectName, is, Files.size(tempVideo), file.getContentType());
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
                } catch (IOException ignored) {}
            }
        }
    }
}