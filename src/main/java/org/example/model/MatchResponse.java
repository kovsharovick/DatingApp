package org.example.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {
    private Long matchId;
    private Long partnerId;
    private String partnerName;
    private int partnerAge;
    private String partnerCity;
    private LocalDateTime matchedAt;
    private String lastMessagePreview;
}