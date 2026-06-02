package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserData;
import org.example.repository.UserDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final MinioService minioService;
    private final UserDataRepository userRepository;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Transactional
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported or missing file type. Allowed: JPEG, PNG, WebP.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Avatar file size must not exceed 5 MB");
        }

        UserData user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            try {
                minioService.deleteFile(user.getAvatarUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar for user {}", userId, e);
            }
        }

        String ext = switch (Objects.requireNonNull(file.getContentType())) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String objectName = "avatars/user" + userId + "/" + UUID.randomUUID() + ext;

        try {
            minioService.uploadFile(objectName, file.getInputStream(),
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }

        user.setAvatarUrl(objectName);
        userRepository.save(user);

        return minioService.getPresignedUrl(objectName);
    }

    @Transactional
    public void deleteAvatar(Long userId) {
        UserData user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAvatarUrl() != null) {
            try {
                minioService.deleteFile(user.getAvatarUrl());
            } catch (Exception e) {
                log.warn("Failed to delete avatar for user {}", userId, e);
            }
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }
}