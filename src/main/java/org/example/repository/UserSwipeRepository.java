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

    boolean existsBySwiper_IdAndTarget_Id(Long swiperId, Long targetId);

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM user_swipes
            WHERE swiper_id = :swiperId
              AND target_id = :targetId
              AND direction = CAST(:direction AS public."SWIPE")
            """, nativeQuery = true)
    boolean existsBySwiper_IdAndTarget_IdAndDirection(
            @Param("swiperId") Long swiperId,
            @Param("targetId") Long targetId,
            @Param("direction") String direction);

    @Query(value = """
            SELECT * FROM user_swipes
            WHERE swiper_id = :swiperId
              AND target_id = :targetId
              AND direction = CAST(:direction AS public."SWIPE")
            LIMIT 1
            """, nativeQuery = true)
    Optional<UserSwipe> findBySwiper_IdAndTarget_IdAndDirection(
            @Param("swiperId") Long swiperId,
            @Param("targetId") Long targetId,
            @Param("direction") String direction);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM user_swipes
            WHERE target_id = :targetId
              AND direction = CAST(:direction AS public."SWIPE")
            """, nativeQuery = true)
    void deleteByTargetIdAndDirection(
            @Param("targetId") Long targetId,
            @Param("direction") String direction);

    @Query(value = """
            SELECT DISTINCT sw.swiper_id
            FROM user_swipes sw
            WHERE sw.target_id = :targetId
              AND sw.direction = CAST(:direction AS public."SWIPE")
              AND sw.swiper_id IN (:candidateIds)
            """, nativeQuery = true)
    Set<Long> findDistinctSwiperIdByTargetIdAndDirectionAndSwiperIdIn(
            @Param("targetId") Long targetId,
            @Param("direction") String direction,
            @Param("candidateIds") Collection<Long> candidateIds);
}