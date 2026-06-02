package org.example.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedItem {
    private Long userId;
    private String name;
    private int age;
    private String city;
    private String region;
    private String videoUrl;
    private String description;
    private boolean likedYou;
    private String avatarUrl;
    private String thumbnailUrl;
}