package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.model.SubscriptionPlan;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserData user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnTransformer(write = "CAST(? AS public.\"SUBSCRIPTION_PLAN\")")
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        this.startedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        if (plan == SubscriptionPlan.FREE) return true;
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    public SubscriptionPlan getEffectivePlan() {
        if (plan == SubscriptionPlan.PREMIUM && isActive()) {
            return SubscriptionPlan.PREMIUM;
        }
        return SubscriptionPlan.FREE;
    }
}