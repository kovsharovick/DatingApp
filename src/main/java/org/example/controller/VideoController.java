package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.VideoResponse;
import org.example.model.VideoUploadResponse;
import org.example.service.VideoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Загрузка видео-анкеты")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            Principal principal,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(principal.getName());
        Long videoId = videoService.uploadVideo(file, userId);
        return ResponseEntity.ok(VideoUploadResponse.builder()
                .videoId(videoId)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<List<VideoResponse>> getMyVideos(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(videoService.getMyVideos(userId));
    }
}
