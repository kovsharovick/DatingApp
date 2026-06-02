package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserBlock;
import org.example.model.BlockedUserResponse;
import org.example.repository.UserBlockRepository;
import org.example.repository.UserDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService {

    private final UserBlockRepository blockRepository;
    private final UserDataRepository userDataRepository;
    private final MinioService minioService;

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }
        if (blockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId)) {
            throw new IllegalStateException("Already blocked");
        }
        UserBlock block = UserBlock.builder()
                .blocker(userDataRepository.getReferenceById(blockerId))
                .blocked(userDataRepository.getReferenceById(blockedId))
                .build();
        blockRepository.save(block);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        blockRepository.findByBlocker_IdAndBlocked_Id(blockerId, blockedId)
                .ifPresent(blockRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(Long blockerId) {
        return blockRepository.findByBlockerId(blockerId)
                .stream()
                .map(block -> {
                    var blocked = block.getBlocked();
                    String avatarUrl = null;
                    try {
                        if (blocked.getAvatarUrl() != null && !blocked.getAvatarUrl().isBlank()) {
                            avatarUrl = minioService.getPresignedUrl(blocked.getAvatarUrl());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to resolve avatar for blocked user {}", blocked.getId(), e);
                    }
                    return BlockedUserResponse.builder()
                            .userId(blocked.getId())
                            .name(blocked.getName())
                            .avatarUrl(avatarUrl)
                            .blockedAt(block.getBlockedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
