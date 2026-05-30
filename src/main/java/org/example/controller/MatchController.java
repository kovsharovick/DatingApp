package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.MatchResponse;
import org.example.entities.Match;
import org.example.entities.UserData;
import org.example.repository.MatchRepository;
import org.example.repository.MessageRepository;
import org.example.service.MinioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Список матчей пользователя")
public class MatchController {

    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getMatches(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        List<Match> matches = matchRepository.findAllByUserId(userId);

        List<MatchResponse> response = matches.stream().map(match -> {
            UserData partner = match.getUser1().getId().equals(userId)
                    ? match.getUser2()
                    : match.getUser1();

            String lastMessage = messageRepository
                    .findTopByMatchIdOrderBySentAtDesc(match.getId())
                    .map(m -> m.getContent().length() > 50
                            ? m.getContent().substring(0, 47) + "..."
                            : m.getContent())
                    .orElse(null);

            String avatarUrl = null;
            try {
                if (partner.getAvatarUrl() != null && !partner.getAvatarUrl().isBlank()) {
                    avatarUrl = minioService.getPresignedUrl(partner.getAvatarUrl());
                }
            } catch (Exception ignored) {
            }

            return MatchResponse.builder()
                    .matchId(match.getId())
                    .partnerId(partner.getId())
                    .partnerName(partner.getName())
                    .partnerAge(Period.between(partner.getDateOfBirth(), LocalDate.now()).getYears())
                    .partnerCity(partner.getCity().getCity())
                    .matchedAt(match.getMatchedAt())
                    .lastMessagePreview(lastMessage)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}