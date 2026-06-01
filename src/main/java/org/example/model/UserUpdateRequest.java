package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;
import org.example.validation.ValidAgeRange;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidAgeRange
public class UserUpdateRequest {

    @Size(min = 1, message = "Name must not be empty")
    @Size(max = 100)
    private String name;

    private String dateOfBirth;

    private Gender gender;

    @Size(max = 150)
    private String city;

    @Size(max = 500)
    private String description;

    private Boolean hidden;

    @Min(value = 18, message = "minAge must be at least 18")
    @Max(value = 99, message = "minAge must be at most 99")
    private Integer minAge;

    @Min(value = 18, message = "maxAge must be at least 18")
    @Max(value = 99, message = "maxAge must be at most 99")
    private Integer maxAge;

    @Min(value = 1, message = "radiusKm must be positive")
    @Max(value = 500, message = "radiusKm must not exceed 500")
    private Integer radiusKm;

    private List<Gender> preferredGenders;
}