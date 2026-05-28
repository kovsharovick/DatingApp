package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.Match;
import org.example.entities.Message;
import org.example.model.MessageResponse;
import org.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final UserDataRepository userDataRepository;
    private final UserBlockRepository blockRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, Long matchId, String content) {
        Match match = getValidatedMatch(senderId, matchId);

        Message message = Message.builder()
                .match(match)
                .sender(userDataRepository.getReferenceById(senderId))
                .content(content)
                .build();

        return toResponse(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(Long userId, Long matchId) {
        getValidatedMatch(userId, matchId);

        return messageRepository.findByMatchIdOrderBySentAtAsc(matchId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Match getValidatedMatch(Long userId, Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        Long user1 = match.getUser1().getId();
        Long user2 = match.getUser2().getId();
        if (!user1.equals(userId) && !user2.equals(userId)) {
            throw new RuntimeException("You are not a member of this match");
        }

        Long partnerId = user1.equals(userId) ? user2 : user1;

        if (blockRepository.existsByBlocker_IdAndBlocked_Id(userId, partnerId)) {
            throw new RuntimeException("Chat is unavailable: you have blocked this user");
        }
        if (blockRepository.existsByBlocker_IdAndBlocked_Id(partnerId, userId)) {
            throw new RuntimeException("Chat is unavailable: you have been blocked");
        }

        return match;
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}