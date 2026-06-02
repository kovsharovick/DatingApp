package org.example.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer durationSec;
    private boolean active;
    private int viewsCount;
    private int likesCount;
    private LocalDateTime createdAt;
}
