package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.UserSwipe;
import org.example.model.SwipeDirection;
import org.example.repository.UserDataRepository;
import org.example.repository.UserSwipeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SwipeTransactionHelper {

    private final UserSwipeRepository swipeRepository;
    private final UserDataRepository userDataRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertSwipe(Long swiperId, Long targetId, SwipeDirection direction) {
        UserSwipe swipe = UserSwipe.builder()
                .swiper(userDataRepository.getReferenceById(swiperId))
                .target(userDataRepository.getReferenceById(targetId))
                .direction(direction)
                .swipedAt(LocalDateTime.now())
                .build();
        try {
            swipeRepository.saveAndFlush(swipe);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Already swiped this user");
        }
    }
}