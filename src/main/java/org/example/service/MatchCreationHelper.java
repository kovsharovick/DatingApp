package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.Match;
import org.example.model.SwipeDirection;
import org.example.repository.AdvisoryLockRepository;
import org.example.repository.MatchRepository;
import org.example.repository.UserDataRepository;
import org.example.repository.UserSwipeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchCreationHelper {

    private final UserSwipeRepository swipeRepository;
    private final UserDataRepository userDataRepository;
    private final MatchRepository matchRepository;
    private final AdvisoryLockRepository advisoryLock;

    @Transactional
    public boolean checkAndCreateMatch(Long currentUserId, Long targetUserId) {
        Long u1 = Math.min(currentUserId, targetUserId);
        Long u2 = Math.max(currentUserId, targetUserId);

        long lockKey = u1 * 1_000_000_000L + u2;
        advisoryLock.acquireTransactionLock(lockKey);

        if (matchRepository.existsByUser1IdAndUser2Id(u1, u2)) {
            return true;
        }

        boolean mutualLike = swipeRepository.existsBySwiper_IdAndTarget_IdAndDirection(
                targetUserId, currentUserId, SwipeDirection.LIKE.name());

        if (!mutualLike) return false;

        Match match = Match.builder()
                .user1(userDataRepository.getReferenceById(u1))
                .user2(userDataRepository.getReferenceById(u2))
                .build();
        matchRepository.save(match);

        log.info("Match created between {} and {}", u1, u2);
        return true;
    }
}