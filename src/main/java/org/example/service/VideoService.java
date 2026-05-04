package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.UserData;
import org.example.entities.UserVideo;
import org.example.repository.UserDataRepository;
import org.example.repository.UserVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final MinioService minioService;
    private final UserVideoRepository videoRepository;
    private final UserDataRepository userRepository;

    @Transactional
    public Long uploadVideo(MultipartFile file, Long userId, int durationSec) {
        UserData user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String objectName = "videos/user" + userId + "/" + UUID.randomUUID() + ".mp4";

        try {
            minioService.uploadFile(objectName, file.getInputStream(),
                                   file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        UserVideo video = UserVideo.builder()
                .user(user)
                .videoUrl(objectName)
                .thumbnailUrl("")
                .durationSec(durationSec)
                .active(true)
                .viewsCount(0)
                .likesCount(0)
                .build();
        video = videoRepository.save(video);

        user.setActiveVideo(video);
        userRepository.save(user);

        return video.getId();
    }
}