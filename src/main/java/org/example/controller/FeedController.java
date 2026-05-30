package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.FeedItem;
import org.example.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Лента пользователей для свайпов")
public class FeedController {

    private final FeedService feedService;
    @GetMapping
    public ResponseEntity<List<FeedItem>> getFeed(
            Principal principal,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(feedService.getFeed(userId, limit));
    }
}