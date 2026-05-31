package org.example.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedUserResponse {
    private Long userId;
    private String name;
    private String avatarUrl;
    private LocalDateTime blockedAt;
}
