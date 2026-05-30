package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.entities.Subscription;
import org.example.model.SubscriptionPlan;
import org.example.service.LikeLimitService;
import org.example.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Управление подпиской")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final LikeLimitService likeLimitService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSubscription(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        Subscription sub = subscriptionService.getSubscription(userId);
        SubscriptionPlan effective = subscriptionService.getEffectivePlan(userId);
        int likesRemaining = likeLimitService.remaining(userId);

        return ResponseEntity.ok(Map.of(
                "plan", sub.getPlan(),
                "effectivePlan", effective,
                "startedAt", sub.getStartedAt(),
                "expiresAt", sub.getExpiresAt() != null ? sub.getExpiresAt() : "never",
                "likesRemaining", likesRemaining
        ));
    }

    @PostMapping("/premium")
    public ResponseEntity<Map<String, String>> activatePremium(
            Principal principal,
            @RequestParam(defaultValue = "30") int days) {
        Long userId = Long.parseLong(principal.getName());
        subscriptionService.activatePremium(userId, days);
        return ResponseEntity.ok(Map.of("message", "Premium activated for " + days + " days"));
    }
}