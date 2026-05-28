package org.example.repository;

import org.example.entities.UserSwipe;
import org.example.model.SwipeDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {

    // проверка, свайпал ли уже пользователь
    boolean existsBySwiper_IdAndTarget_Id(Long swiperId, Long targetId);

    // проверка существования конкретного направления
    boolean existsBySwiper_IdAndTarget_IdAndDirection(Long swiperId, Long targetId, SwipeDirection direction);

    // найти конкретный лайк/дизлайк
    Optional<UserSwipe> findBySwiper_IdAndTarget_IdAndDirection(
            Long swiperId, Long targetId, SwipeDirection direction);

    // удаление всех дизлайков, направленных на пользователя
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSwipe s WHERE s.target.id = :targetId AND s.direction = :direction")
    void deleteByTargetIdAndDirection(@Param("targetId") Long targetId, @Param("direction") SwipeDirection direction);

    @Query("SELECT DISTINCT sw.swiper.id FROM UserSwipe sw WHERE sw.target.id = :targetId AND sw.direction = :direction AND sw.swiper.id IN :candidateIds")
    Set<Long> findDistinctSwiperIdByTargetIdAndDirectionAndSwiperIdIn(
            @Param("targetId") Long targetId,
            @Param("direction") SwipeDirection direction,
            @Param("candidateIds") Collection<Long> candidateIds);
}