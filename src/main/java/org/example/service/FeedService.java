package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserData;
import org.example.model.FeedItem;
import org.example.model.Gender;
import org.example.repository.UserDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public List<FeedItem> getFeed(Long userId, int limit) {
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

        List<Object[]> priorityRows = userDataRepository.findPriorityCandidates(
                userId, lat, lon, (double) radiusM, minBirth, maxBirth,
                currentUser.getDateOfBirth(), prefs, limit);
        if (!priorityRows.isEmpty()) {
            return mapToFeedItems(priorityRows, true);
        }

        List<Object[]> regularRows = userDataRepository.findRegularCandidates(
                userId, lat, lon, (double) radiusM, minBirth, maxBirth,
                currentUser.getDateOfBirth(), prefs, limit);
        if (!regularRows.isEmpty()) {
            return mapToFeedItems(regularRows, false);
        }

        // расширение границ
        double expandedRadiusM = radiusM * 1.5;
        LocalDate expandedMinBirth = minBirth.minusYears(5);
        LocalDate expandedMaxBirth = maxBirth.plusYears(5);
        regularRows = userDataRepository.findRegularCandidates(
                userId, lat, lon, expandedRadiusM, expandedMinBirth, expandedMaxBirth,
                currentUser.getDateOfBirth(), prefs, limit);
        if (!regularRows.isEmpty()) {
            return mapToFeedItems(regularRows, false);
        }

        // почти без ограничений
        regularRows = userDataRepository.findRegularCandidates(
                userId, lat, lon, 10_000_000.0,
                LocalDate.now().minusYears(120), LocalDate.now().minusYears(18),
                currentUser.getDateOfBirth(), prefs, limit);
        if (!regularRows.isEmpty()) {
            return mapToFeedItems(regularRows, false);
        }

        List<Object[]> list = new ArrayList<>();
        return mapToFeedItems(list, false);
        // без ограничений
        //List<Object[]> fallbackRows = userDataRepository.findFallbackCandidates(userId, limit);
        //return mapToFeedItems(fallbackRows, false);
    }


    //"{\"MALE\",\"FEMALE\"}"
    private String buildPrefsLiteral(List<Gender> genders) {
        if (genders == null || genders.isEmpty()) return null;
        String values = genders.stream()
                .map(Gender::name)
                .collect(Collectors.joining("\",\"", "{\"", "\"}"));
        return values;
    }

    private List<FeedItem> mapToFeedItems(List<Object[]> rows, boolean likedYou) {
        if (rows.isEmpty()) return Collections.emptyList();

        List<Long> ids = rows.stream()
                .map(r -> ((Number) r[0]).longValue())
                .collect(Collectors.toList());

        List<UserData> users = userDataRepository.findAllByIdWithCityAndVideo(ids);
        Map<Long, UserData> userMap = users.stream()
                .collect(Collectors.toMap(UserData::getId, Function.identity()));

        return rows.stream()
                .map(row -> {
                    Long id = ((Number) row[0]).longValue();
                    String videoPath = (String) row[1];
                    UserData user = userMap.get(id);
                    if (user == null) return null;

                    String presignedUrl = null;
                    if (videoPath != null) {
                        try {
                            presignedUrl = minioService.getPresignedUrl(videoPath);
                        } catch (Exception e) {
                            log.error("Failed to get presigned URL for video {}", videoPath, e);
                            return null;
                        }
                    } else {
                        return null;
                    }

                    return FeedItem.builder()
                            .userId(id)
                            .name(user.getName())
                            .age(Period.between(user.getDateOfBirth(), LocalDate.now()).getYears())
                            .city(user.getCity().getCity())
                            .region(user.getCity().getRegion())
                            .description(user.getDescription())
                            .videoUrl(presignedUrl)
                            .likedYou(likedYou)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}