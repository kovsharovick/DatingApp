package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.MessageRequest;
import org.example.model.MessageResponse;
import org.example.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Чат между пользователями")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{matchId}")
    public void handleWebSocketMessage(@DestinationVariable Long matchId,
                                       @Payload MessageRequest request,
                                       Principal principal) {
        Long senderId = Long.parseLong(principal.getName());

        MessageResponse response = messageService.sendMessage(senderId, matchId, request.getContent());

        messagingTemplate.convertAndSend("/topic/match/" + matchId, response);
    }

    @GetMapping("/api/messages/{matchId}/history")
    public ResponseEntity<List<MessageResponse>> getHistory(@PathVariable Long matchId,
                                                            Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        List<MessageResponse> history = messageService.getHistory(userId, matchId);
        return ResponseEntity.ok(history);
    }
}