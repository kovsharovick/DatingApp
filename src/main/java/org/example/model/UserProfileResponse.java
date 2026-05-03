package org.example.model;

import lombok.*;

import java.util.List;

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
    private Integer minAge;
    private Integer maxAge;
    private Integer radiusKm;
    private List<Gender> preferredGenders;
}