package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserData;
import org.example.model.FeedItem;
import org.example.model.Gender;
import org.example.model.SwipeDirection;
import org.example.repository.UserDataRepository;
import org.example.repository.UserSwipeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final UserDataRepository userDataRepository;
    private final UserSwipeRepository userSwipeRepository;
    private final MinioService minioService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    private static final Duration FEED_IDS_TTL = Duration.ofMinutes(5);

    private String feedIdsKey(Long userId) {
        return "feed:ids:" + userId;
    }

    public void invalidateFeedCache(Long userId) {
        redisTemplate.delete(feedIdsKey(userId));
        log.debug("Feed ID cache invalidated for user {}", userId);
    }

    public void removeSwipedUserId(Long userId, Long targetUserId) {
        String key = feedIdsKey(userId);
        Long removed = redisTemplate.opsForList().remove(key, 1, targetUserId.intValue() <= Integer.MAX_VALUE
                ? targetUserId.intValue()
                : targetUserId);
        if (removed == null || removed == 0) {
            redisTemplate.opsForList().remove(key, 1, targetUserId);
        }
        log.debug("Attempted removal of user {} from feed cache of user {}", targetUserId, userId);
    }

    @Transactional(readOnly = true)
    public List<FeedItem> getFeed(Long userId, int limit) {
        List<Long> allIds = getCandidateIds(userId, limit);
        if (allIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idsToShow = allIds.stream()
                .limit(limit)
                .collect(Collectors.toList());

        Map<Long, UserData> userMap = userDataRepository
                .findAllByIdWithCityAndVideo(idsToShow)
                .stream()
                .collect(Collectors.toMap(UserData::getId, Function.identity()));

        Set<Long> likedYouSet = getUsersWhoLikedMe(userId, idsToShow);

        return idsToShow.stream()
                .map(id -> buildFeedItem(userMap.get(id), likedYouSet.contains(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Long> getCandidateIds(Long userId, int limit) {
        String key = feedIdsKey(userId);
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw != null && !raw.isEmpty()) {
            return raw.stream()
                    .map(o -> ((Number) o).longValue())
                    .collect(Collectors.toList());
        }

        List<Long> ids = loadCandidateIdsFromDb(userId, limit);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        redisTemplate.opsForList().rightPushAll(key, ids.toArray());
        redisTemplate.expire(key, FEED_IDS_TTL);
        return ids;
    }

    private List<Long> loadCandidateIdsFromDb(Long userId, int limit) {
        UserData currentUser = userDataRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getActiveVideo() == null) {
            throw new RuntimeException("You must upload a video before browsing the feed.");
        }

        double lat = currentUser.getCity().getLatitude();
        double lon = currentUser.getCity().getLongitude();
        int radiusM = (currentUser.getRadiusKm() != null ? currentUser.getRadiusKm() : 50) * 1000;
        LocalDate now = LocalDate.now();
        LocalDate minBirth = now.minusYears(currentUser.getMaxAge() != null ? currentUser.getMaxAge() : 99);
        LocalDate maxBirth = now.minusYears(currentUser.getMinAge() != null ? currentUser.getMinAge() : 18);
        String prefs = buildPrefsLiteral(currentUser.getPreferredGenders());

        List<Object[]> rows = userDataRepository.findPriorityCandidates(
                userId, lat, lon, (double) radiusM, minBirth, maxBirth,
                currentUser.getDateOfBirth(), prefs, limit);
        if (!rows.isEmpty()) return extractIds(rows);

        rows = userDataRepository.findRegularCandidates(
                userId, lat, lon, (double) radiusM, minBirth, maxBirth,
                currentUser.getDateOfBirth(), prefs, limit);
        if (!rows.isEmpty()) return extractIds(rows);

        return Collections.emptyList();
    }

    private List<Long> extractIds(List<Object[]> rows) {
        return rows.stream()
                .map(r -> ((Number) r[0]).longValue())
                .collect(Collectors.toList());
    }

    private String buildPrefsLiteral(List<Gender> genders) {
        if (genders == null || genders.isEmpty()) return null;
        return genders.stream()
                .map(Gender::name)
                .collect(Collectors.joining("\",\"", "{\"", "\"}"));
    }

    private Set<Long> getUsersWhoLikedMe(Long currentUserId, List<Long> candidateIds) {
        if (candidateIds.isEmpty()) return Set.of();
        return userSwipeRepository.findDistinctSwiperIdByTargetIdAndDirectionAndSwiperIdIn(
                currentUserId, SwipeDirection.LIKE.name(), candidateIds);
    }

    private FeedItem buildFeedItem(UserData user, boolean likedYou) {
        if (user == null) return null;

        String presignedVideoUrl = null;
        try {
            if (user.getActiveVideo() != null) {
                presignedVideoUrl = minioService.getPresignedUrl(user.getActiveVideo().getVideoUrl());
            }
        } catch (Exception e) {
            log.error("Failed to get video URL for user {}", user.getId(), e);
            return null;
        }

        String thumbnailUrl = null;
        try {
            if (user.getActiveVideo() != null && user.getActiveVideo().getThumbnailUrl() != null
                    && !user.getActiveVideo().getThumbnailUrl().isBlank()) {
                thumbnailUrl = minioService.getPresignedUrl(user.getActiveVideo().getThumbnailUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to get thumbnail URL for user {}", user.getId(), e);
        }

        String avatarUrl = null;
        try {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
                avatarUrl = minioService.getPresignedUrl(user.getAvatarUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to get avatar URL for user {}", user.getId(), e);
        }

        return FeedItem.builder()
                .userId(user.getId())
                .name(user.getName())
                .age(Period.between(user.getDateOfBirth(), LocalDate.now()).getYears())
                .city(user.getCity().getCity())
                .region(user.getCity().getRegion())
                .description(user.getDescription())
                .videoUrl(presignedVideoUrl)
                .thumbnailUrl(thumbnailUrl)
                .avatarUrl(avatarUrl)
                .likedYou(likedYou)
                .build();
    }
}