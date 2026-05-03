package org.example.repository;

import org.example.entities.UserSwipe;
import org.example.model.SwipeDirection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {

    // проверка, свайпал ли уже пользователь
    boolean existsBySwiper_IdAndTarget_Id(Long swiperId, Long targetId);

    // найти конкретный лайк
    Optional<UserSwipe> findBySwiper_IdAndTarget_IdAndDirection(
            Long swiperId, Long targetId, SwipeDirection direction);
}