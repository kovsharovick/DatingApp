package org.example.entities;

import lombok.*;
import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "user_blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private UserData blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private UserData blocked;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    @PrePersist
    public void prePersist() {
        this.blockedAt = LocalDateTime.now();
    }
}