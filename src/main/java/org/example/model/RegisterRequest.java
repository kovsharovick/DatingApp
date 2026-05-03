package org.example.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private String dateOfBirth;

    @NotNull
    private Gender gender;

    @NotBlank
    private String city;

    @Size(max = 500)
    private String description;

    private Integer minAge;

    private Integer maxAge;

    private Integer radiusKm;

    private List<Gender> preferredGenders;
}