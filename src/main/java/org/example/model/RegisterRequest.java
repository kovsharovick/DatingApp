package org.example.model;

import jakarta.validation.constraints.*;
import lombok.*;

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
}