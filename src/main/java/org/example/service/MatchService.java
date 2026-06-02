package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.Match;
import org.example.entities.UserData;
import org.example.model.MatchResponse;
import org.example.repository.MatchRepository;
import org.example.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(Long userId) {
        return matchRepository.findAllByUserId(userId)
                .stream()
                .map(match -> toResponse(match, userId))
                .collect(Collectors.toList());
    }

    private MatchResponse toResponse(Match match, Long userId) {
        UserData partner = match.getUser1().getId().equals(userId)
                ? match.getUser2()
                : match.getUser1();

        String lastMessage = messageRepository
                .findTopByMatchIdOrderBySentAtDesc(match.getId())
                .map(m -> m.getContent().length() > 50
                        ? m.getContent().substring(0, 47) + "..."
                        : m.getContent())
                .orElse(null);

        String avatarUrl = resolvePresignedUrl(partner.getAvatarUrl(), partner.getId(), "avatar");

        return MatchResponse.builder()
                .matchId(match.getId())
                .partnerId(partner.getId())
                .partnerName(partner.getName())
                .partnerAge(Period.between(partner.getDateOfBirth(), LocalDate.now()).getYears())
                .partnerCity(partner.getCity().getCity())
                .partnerAvatarUrl(avatarUrl)
                .matchedAt(match.getMatchedAt())
                .lastMessagePreview(lastMessage)
                .build();
    }

    private String resolvePresignedUrl(String objectName, Long userId, String kind) {
        if (objectName == null || objectName.isBlank()) return null;
        try {
            return minioService.getPresignedUrl(objectName);
        } catch (Exception e) {
            log.warn("Failed to get {} URL for user {}", kind, userId, e);
            return null;
        }
    }
}
