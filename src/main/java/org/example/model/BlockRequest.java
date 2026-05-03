package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockRequest {

    @NotNull
    private Long blockedUserId;
}