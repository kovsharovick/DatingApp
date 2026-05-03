package org.example.entities;

import lombok.*;
import org.example.model.SwipeDirection;
import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "user_swipes", uniqueConstraints = @UniqueConstraint(columnNames = {"swiper_id", "target_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSwipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    private UserData swiper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private UserData target;

    @Column(name = "swiped_at", nullable = false)
    private LocalDateTime swipedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwipeDirection direction;

    @PrePersist
    public void prePersist() {
        this.swipedAt = LocalDateTime.now();
    }
}