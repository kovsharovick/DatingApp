package org.example.repository;

import org.example.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // история сообщений внутри одного мэтча, от старых к новым
    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);

    // последнее сообщение в мэтче
    Optional<Message> findTopByMatchIdOrderBySentAtDesc(Long matchId);
}