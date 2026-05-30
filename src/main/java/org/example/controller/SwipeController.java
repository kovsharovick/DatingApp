package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.ApiResponse;
import org.example.model.SwipeRequest;
import org.example.service.SwipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/swipes")
@RequiredArgsConstructor
@Tag(name = "Swipes", description = "Свайпы пользователей")
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> swipe(
            Principal principal,
            @Valid @RequestBody SwipeRequest request) {
        Long userId = Long.parseLong(principal.getName());
        boolean matched = swipeService.swipe(userId, request.getTargetUserId(), request.getDirection());
        String message = matched ? "It's a match!" : "Swipe recorded";
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(message)
                .data(matched)
                .build());
    }
}