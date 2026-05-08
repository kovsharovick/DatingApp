package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank @Size(max = 100)
    private String name;

    private String dateOfBirth;

    private Gender gender;

    @Size(max = 150)
    private String city;

    @Size(max = 500)
    private String description;

    private Boolean hidden;

    private Integer minAge;

    private Integer maxAge;

    private Integer radiusKm;

    private List<Gender> preferredGenders;
}