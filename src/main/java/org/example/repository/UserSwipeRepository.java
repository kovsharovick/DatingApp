package org.example.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.example.entities.UserSwipe;
import org.example.model.SwipeDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {

    // проверка, свайпал ли уже пользователь
    boolean existsBySwiper_IdAndTarget_Id(Long swiperId, Long targetId);

    // найти конкретный лайк
    Optional<UserSwipe> findBySwiper_IdAndTarget_IdAndDirection(
            Long swiperId, Long targetId, SwipeDirection direction);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSwipe s WHERE s.target.id = :targetId AND s.direction = :direction")
    void deleteByTargetIdAndDirection(@Param("targetId") Long targetId, @Param("direction") SwipeDirection direction);
}