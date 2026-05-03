package org.example.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private int age;
    private String city;
    private String region;
    private String description;
    private String videoUrl;
    private boolean hidden;
}