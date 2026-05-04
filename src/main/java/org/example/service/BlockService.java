package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.UserBlock;
import org.example.repository.UserBlockRepository;
import org.example.repository.UserDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserBlockRepository blockRepository;
    private final UserDataRepository userDataRepository;

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
}