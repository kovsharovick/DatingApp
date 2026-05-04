package org.example.entities;

import lombok.*;
import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "user_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserData user;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    //nullable = false
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "views_count", nullable = false)
    private int viewsCount = 0;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}