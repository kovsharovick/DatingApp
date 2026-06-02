package org.example.model;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotNull
    private Long reportedUserId;

    @NotBlank
    @Size(max = 255)
    private String reason;
}