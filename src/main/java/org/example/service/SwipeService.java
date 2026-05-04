package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.Match;
import org.example.entities.UserData;
import org.example.entities.UserSwipe;
import org.example.model.SwipeDirection;
import org.example.repository.MatchRepository;
import org.example.repository.UserDataRepository;
import org.example.repository.UserSwipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwipeService {

    private final UserSwipeRepository swipeRepository;
    private final UserDataRepository userDataRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public boolean swipe(Long currentUserId, Long targetUserId, SwipeDirection direction) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot swipe yourself");
        }

        if (swipeRepository.existsBySwiper_IdAndTarget_Id(currentUserId, targetUserId)) {
            throw new IllegalStateException("Already swiped this user");
        }

        UserSwipe swipe = UserSwipe.builder()
                .swiper(userDataRepository.getReferenceById(currentUserId))
                .target(userDataRepository.getReferenceById(targetUserId))
                .direction(direction)
                .swipedAt(LocalDateTime.now())
                .build();
        swipeRepository.save(swipe);

        if (direction == SwipeDirection.DISLIKE) {
            return false;
        }

        boolean mutualLike = swipeRepository.existsBySwiper_IdAndTarget_IdAndDirection(
                targetUserId, currentUserId, SwipeDirection.LIKE);

        if (mutualLike) {
            Long user1Id = currentUserId < targetUserId ? currentUserId : targetUserId;
            Long user2Id = currentUserId < targetUserId ? targetUserId : currentUserId;

            Match match = Match.builder()
                    .user1(userDataRepository.getReferenceById(user1Id))
                    .user2(userDataRepository.getReferenceById(user2Id))
                    .matchedAt(LocalDateTime.now())
                    .build();
            matchRepository.save(match);
            log.info("New match created between {} and {}", user1Id, user2Id);
            return true;
        }

        return false;
    }
}