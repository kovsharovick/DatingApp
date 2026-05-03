package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull
    private Long matchId;

    @NotBlank
    @Size(max = 1000)
    private String content;
}