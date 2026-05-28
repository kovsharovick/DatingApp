package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailService {

    private final MinioService minioService;

    public String generateAndUpload(String videoObjectName, Long userId) {
        Path tempVideo = null;
        Path tempThumb = null;

        try {
            tempVideo = Files.createTempFile("video_", ".mp4");
            try (InputStream stream = minioService.downloadFile(videoObjectName)) {
                Files.copy(stream, tempVideo,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            tempThumb = Files.createTempFile("thumb_", ".jpg");

            int exitCode = getExitCode(tempVideo, tempThumb);

            if (exitCode != 0) {
                log.error("FFmpeg failed with exit code {} for video {}", exitCode, videoObjectName);
                return null;
            }

            String thumbObjectName = "thumbnails/user" + userId + "/"
                    + UUID.randomUUID() + ".jpg";
            long size = Files.size(tempThumb);
            try (InputStream thumbStream = Files.newInputStream(tempThumb)) {
                minioService.uploadFile(thumbObjectName, thumbStream, size, "image/jpeg");
            }

            return thumbObjectName;

        } catch (IOException | InterruptedException e) {
            log.error("Thumbnail generation failed for video {}", videoObjectName, e);
            return null;
        } finally {
            deleteSilently(tempVideo);
            deleteSilently(tempThumb);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<String> generateAndUploadAsync(String videoObjectName, Long userId) {
        String thumbnailUrl = generateAndUpload(videoObjectName, userId);
        return CompletableFuture.completedFuture(thumbnailUrl);
    }

    private static int getExitCode(Path tempVideo, Path tempThumb) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-ss", "00:00:01",
                "-i", tempVideo.toAbsolutePath().toString(),
                "-vframes", "1",
                "-vf", "scale=480:270:force_original_aspect_ratio=decrease",
                "-q:v", "2",
                tempThumb.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        return process.waitFor();
    }

    private void deleteSilently(Path path) {
        try {
            if (path != null) Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}