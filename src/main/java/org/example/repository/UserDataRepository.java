package org.example.repository;

import org.springframework.data.repository.query.Param;
import org.example.entities.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByEmail(String email);

    boolean existsByEmail(String email);

    // приоритетная очередь
    @Query(value = """
                SELECT u.id, v.video_url
                FROM user_data u
                JOIN city c ON u.city_id = c.id
                LEFT JOIN user_videos v ON u.active_video_id = v.id
                WHERE u.id IN (
                    SELECT sw.swiper_id FROM user_swipes sw
                    WHERE sw.target_id = :currentUserId AND sw.direction = 'LIKE'
                )
                AND u.is_hidden = FALSE
                AND u.active_video_id IS NOT NULL
                AND u.id <> :currentUserId
                AND (:prefs IS NULL OR u.gender = ANY(CAST(:prefs AS public."GENDER"[])))
                AND u.date_of_birth BETWEEN :minBirth AND :maxBirth
                AND earth_distance(ll_to_earth(c.latitude, c.longitude), ll_to_earth(:lat, :lon)) <= :radiusMetres
                AND u.id NOT IN (SELECT blocked_id FROM user_blocks WHERE blocker_id = :currentUserId)
                AND u.id NOT IN (SELECT blocker_id FROM user_blocks WHERE blocked_id = :currentUserId)
                ORDER BY
                    ABS(DATE_PART('year', AGE(u.date_of_birth, :currentBirth))) ASC,
                    earth_distance(ll_to_earth(c.latitude, c.longitude), ll_to_earth(:lat, :lon)) ASC
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findPriorityCandidates(
            @Param("currentUserId") Long currentUserId,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusMetres") Double radiusMetres,
            @Param("minBirth") LocalDate minBirth,
            @Param("maxBirth") LocalDate maxBirth,
            @Param("currentBirth") LocalDate currentBirth,
            @Param("prefs") String prefs,
            @Param("limit") int limit
    );

    // обычная очередь
    @Query(value = """
                SELECT u.id, v.video_url
                FROM user_data u
                JOIN city c ON u.city_id = c.id
                LEFT JOIN user_videos v ON u.active_video_id = v.id
                WHERE u.is_hidden = FALSE
                  AND u.active_video_id IS NOT NULL
                  AND u.id <> :currentUserId
                  AND (:prefs IS NULL OR u.gender = ANY(CAST(:prefs AS public."GENDER"[])))
                  AND u.date_of_birth BETWEEN :minBirth AND :maxBirth
                  AND earth_distance(ll_to_earth(c.latitude, c.longitude), ll_to_earth(:lat, :lon)) <= :radiusMetres
                  AND u.id NOT IN (
                      SELECT sw.target_id FROM user_swipes WHERE swiper_id = :currentUserId AND direction = 'LIKE'
                  )
                  AND u.id NOT IN (
                      SELECT d.target_id FROM user_swipes d
                      WHERE d.swiper_id = :currentUserId AND d.direction = 'DISLIKE'
                  )
                  AND u.id NOT IN (SELECT blocked_id FROM user_blocks WHERE blocker_id = :currentUserId)
                  AND u.id NOT IN (SELECT blocker_id FROM user_blocks WHERE blocked_id = :currentUserId)
                ORDER BY 
                    earth_distance(ll_to_earth(c.latitude, c.longitude), ll_to_earth(:lat, :lon)) ASC,
                    ABS(DATE_PART('year', AGE(u.date_of_birth, :currentBirth))) ASC
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findRegularCandidates(
            @Param("currentUserId") Long currentUserId,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusMetres") Double radiusMetres,
            @Param("minBirth") LocalDate minBirth,
            @Param("maxBirth") LocalDate maxBirth,
            @Param("currentBirth") LocalDate currentBirth,
            @Param("prefs") String prefs,
            @Param("limit") int limit
    );

    // запасной (без фильтров расстояния, возраста, пола)
    @Query(value = """
                SELECT u.id, v.video_url
                FROM user_data u
                JOIN city c ON u.city_id = c.id
                LEFT JOIN user_videos v ON u.active_video_id = v.id
                WHERE u.is_hidden = FALSE
                  AND u.active_video_id IS NOT NULL
                  AND u.id <> :currentUserId
                  AND u.id NOT IN (SELECT blocked_id FROM user_blocks WHERE blocker_id = :currentUserId)
                  AND u.id NOT IN (SELECT blocker_id FROM user_blocks WHERE blocked_id = :currentUserId)
                  AND u.id NOT IN (SELECT sw.target_id FROM user_swipes WHERE swiper_id = :currentUserId AND direction = 'LIKE')
                ORDER BY random()
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findFallbackCandidates(
            @Param("currentUserId") Long currentUserId,
            @Param("limit") int limit
    );

    // загрузка полных UserData со связями для ID
    @Query("SELECT u FROM UserData u LEFT JOIN FETCH u.city LEFT JOIN FETCH u.activeVideo WHERE u.id IN :ids")
    List<UserData> findAllByIdWithCityAndVideo(@Param("ids") Collection<Long> ids);
}