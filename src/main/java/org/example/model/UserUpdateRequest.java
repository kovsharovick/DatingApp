package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100)
    private String name;

    private String dateOfBirth;

    private Gender gender;

    @Size(max = 150)
    private String city;

    @Size(max = 500)
    private String description;

    private Boolean hidden;
}