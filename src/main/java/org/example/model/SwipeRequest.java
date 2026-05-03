package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SwipeRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private SwipeDirection direction;
}