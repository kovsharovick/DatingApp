package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.BlockRequest;
import org.example.service.BlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Tag(name = "Blocks", description = "Блокировка пользователей")
public class BlockController {

    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<Void> block(
            Principal principal,
            @Valid @RequestBody BlockRequest request) {
        Long userId = Long.parseLong(principal.getName());
        blockService.blockUser(userId, request.getBlockedUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<Void> unblock(
            Principal principal,
            @PathVariable Long blockedUserId) {
        Long userId = Long.parseLong(principal.getName());
        blockService.unblockUser(userId, blockedUserId);
        return ResponseEntity.noContent().build();
    }
}