package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SubscriptionService subscriptionService;

    @Value("${app.likes.free-daily-limit:5}")
    private int freeDailyLimit;

    @Value("${app.likes.premium-daily-limit:100}")
    private int premiumDailyLimit;

    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>(
            """
                    local current = redis.call('INCR', KEYS[1])
                    if current == 1 then
                        redis.call('PEXPIRE', KEYS[1], ARGV[1])
                    end
                    return current
                    """,
            Long.class
    );

    private String key(Long userId) {
        return "likes:daily:" + userId + ":" + LocalDate.now();
    }

    private int limitFor(Long userId) {
        SubscriptionPlan plan = subscriptionService.getEffectivePlan(userId);
        return plan == SubscriptionPlan.PREMIUM ? premiumDailyLimit : freeDailyLimit;
    }

    private long millisUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).toMillis();
    }

    public boolean isLimitReached(Long userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) return false;
        return ((Number) value).intValue() >= limitFor(userId);
    }

    public void increment(Long userId) {
        redisTemplate.execute(
                INCREMENT_SCRIPT,
                List.of(key(userId)),
                millisUntilMidnight()
        );
    }

    public int remaining(Long userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        int used = value == null ? 0 : ((Number) value).intValue();
        return Math.max(0, limitFor(userId) - used);
    }
}