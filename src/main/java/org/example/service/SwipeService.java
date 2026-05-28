package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.SwipeDirection;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwipeService {

    private final LikeLimitService likeLimitService;
    private final FeedService feedService;
    private final SwipeTransactionHelper swipeHelper;
    private final MatchCreationHelper matchCreationHelper;

    public boolean swipe(Long currentUserId, Long targetUserId, SwipeDirection direction) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot swipe yourself");
        }

        if (direction == SwipeDirection.LIKE
                && likeLimitService.isLimitReached(currentUserId)) {
            throw new IllegalStateException(
                    "Daily like limit reached. Remaining: "
                            + likeLimitService.remaining(currentUserId));
        }

        swipeHelper.insertSwipe(currentUserId, targetUserId, direction);

        feedService.removeSwipedUserId(currentUserId, targetUserId);

        if (direction == SwipeDirection.DISLIKE) return false;

        likeLimitService.increment(currentUserId);

        // Вызов через отдельный компонент — транзакция Spring AOP работает корректно
        return matchCreationHelper.checkAndCreateMatch(currentUserId, targetUserId);
    }
}