package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class LikeLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SubscriptionService subscriptionService;

    @Value("${app.likes.free-daily-limit:5}")
    private int freeDailyLimit;

    @Value("${app.likes.premium-daily-limit:100}")
    private int premiumDailyLimit;

    private String key(Long userId) {
        return "likes:daily:" + userId + ":" + LocalDate.now();
    }

    private int limitFor(Long userId) {
        SubscriptionPlan plan = subscriptionService.getEffectivePlan(userId);
        return plan == SubscriptionPlan.PREMIUM ? premiumDailyLimit : freeDailyLimit;
    }

    public boolean isLimitReached(Long userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) return false;
        return ((Number) value).intValue() >= limitFor(userId);
    }

    public void increment(Long userId) {
        String key = key(userId);
        redisTemplate.opsForValue().increment(key);
        Duration ttl = Duration.between(LocalTime.now(), LocalTime.MIDNIGHT.minusSeconds(1));
        if (ttl.isNegative()) ttl = ttl.plusDays(1);
        redisTemplate.expire(key, ttl);
    }

    public int remaining(Long userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        int used = value == null ? 0 : ((Number) value).intValue();
        return Math.max(0, limitFor(userId) - used);
    }
}