package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.Subscription;
import org.example.model.SubscriptionPlan;
import org.example.repository.SubscriptionRepository;
import org.example.repository.UserDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserDataRepository userDataRepository;

    @Transactional
    public void createFreeSubscription(Long userId) {
        Subscription sub = Subscription.builder()
                .user(userDataRepository.getReferenceById(userId))
                .plan(SubscriptionPlan.FREE)
                .build();
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void activatePremium(Long userId, int durationDays) {
        Subscription sub = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (sub.getExpiresAt() != null && sub.getExpiresAt().isAfter(now))
                ? sub.getExpiresAt()
                : now;

        sub.setPlan(SubscriptionPlan.PREMIUM);
        sub.setStartedAt(now);
        sub.setExpiresAt(base.plusDays(durationDays));
        subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getEffectivePlan(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(Subscription::getEffectivePlan)
                .orElse(SubscriptionPlan.FREE);
    }

    @Transactional(readOnly = true)
    public Subscription getSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }
}