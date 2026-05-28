package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.config.JwtUtil;
import org.example.entities.City;
import org.example.entities.UserData;
import org.example.model.*;
import org.example.repository.CityRepository;
import org.example.repository.UserDataRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MinioService minioService;
    private final CityService cityService;
    private final JwtUtil jwtUtil;
    private final SubscriptionService subscriptionService;
    private final FeedService feedService;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userDataRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        LocalDate dob = LocalDate.parse(request.getDateOfBirth());
        if (Period.between(dob, LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("User must be at least 18 years old");
        }
        City city = cityService.findCityByName(request.getCity())
                .orElseThrow(() -> new RuntimeException("City not found"));

        UserData user = UserData.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .dateOfBirth(dob)
                .city(city)
                .gender(request.getGender())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .minAge(request.getMinAge() != null ? request.getMinAge() : 18)
                .maxAge(request.getMaxAge() != null ? request.getMaxAge() : 99)
                .radiusKm(request.getRadiusKm() != null ? request.getRadiusKm() : 50)
                .build();

        user.setPreferredGenders(request.getPreferredGenders() != null ?
                request.getPreferredGenders() : Collections.emptyList());

        user = userDataRepository.save(user);
        subscriptionService.createFreeSubscription(user.getId());

        String token = jwtUtil.generateToken(user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserData user = userDataRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserData user = userDataRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String videoUrl = null;
        String thumbnailUrl = null;

        if (user.getActiveVideo() != null) {
            try {
                videoUrl = minioService.getPresignedUrl(
                        user.getActiveVideo().getVideoUrl());
            } catch (Exception e) {
                log.error("Failed to get video URL for user {}: {}", userId, e.getMessage());
            }

            try {
                String thumb = user.getActiveVideo().getThumbnailUrl();
                if (thumb != null && !thumb.isBlank()) {
                    thumbnailUrl = minioService.getPresignedUrl(thumb);
                }
            } catch (Exception e) {
                log.error("Failed to get thumbnail URL for user {}: {}", userId, e.getMessage());
            }
        }

        String avatarUrl = null;
        try {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
                avatarUrl = minioService.getPresignedUrl(user.getAvatarUrl());
            }
        } catch (Exception e) {
            log.error("Failed to get avatar URL for user {}: {}", userId, e.getMessage());
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .age(Period.between(user.getDateOfBirth(), LocalDate.now()).getYears())
                .city(user.getCity().getCity())
                .region(user.getCity().getRegion())
                .description(user.getDescription())
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .avatarUrl(avatarUrl)
                .hidden(user.isHidden())
                .minAge(user.getMinAge())
                .maxAge(user.getMaxAge())
                .radiusKm(user.getRadiusKm())
                .preferredGenders(user.getPreferredGenders())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserUpdateRequest request) {
        UserData user = userDataRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getCity() != null && !request.getCity().isEmpty()) {
            City city = cityService.findCityByName(request.getCity())
                    .orElseThrow(() -> new RuntimeException("City not found"));
            user.setCity(city);
        }
        if (request.getDescription() != null) {
            user.setDescription(request.getDescription());
        }
        if (request.getHidden() != null) {
            user.setHidden(request.getHidden());
        }
        if (request.getMinAge() != null) {
            user.setMinAge(request.getMinAge());
        }
        if (request.getMaxAge() != null) {
            user.setMaxAge(request.getMaxAge());
        }
        if (request.getRadiusKm() != null) {
            user.setRadiusKm(request.getRadiusKm());
        }
        if (request.getPreferredGenders() != null) {
            user.setPreferredGenders(request.getPreferredGenders());
        }

        userDataRepository.save(user);

        boolean feedAffected = request.getMinAge() != null
                || request.getMaxAge() != null
                || request.getRadiusKm() != null
                || request.getPreferredGenders() != null
                || (request.getCity() != null && !request.getCity().isEmpty());

        if (feedAffected) {
            feedService.invalidateFeedCache(userId);
            log.debug("Feed cache invalidated for user {} after profile update", userId);
        }

        return getProfile(userId);
    }
}