package org.example.entities;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import lombok.*;
import org.example.model.SwipeDirection;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

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
    @ColumnTransformer(write = "CAST(? AS public.\"SWIPE\")")
    private SwipeDirection direction;

    @PrePersist
    public void prePersist() {
        this.swipedAt = LocalDateTime.now();
    }
}