package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.PublicUserProfileResponse;
import org.example.model.UserProfileResponse;
import org.example.model.UserUpdateRequest;
import org.example.service.AvatarService;
import org.example.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление профилем пользователя")
public class UserController {

    private final UserService userService;
    private final AvatarService avatarService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(
            @PathVariable Long id,
            Principal principal) {
        Long requesterId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(userService.getPublicProfile(id, requesterId));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            Principal principal,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(principal.getName());
        String url = avatarService.uploadAvatar(file, userId);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Void> deleteAvatar(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        avatarService.deleteAvatar(userId);
        return ResponseEntity.noContent().build();
    }
}
