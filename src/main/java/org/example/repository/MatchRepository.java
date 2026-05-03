package org.example.repository;

import org.example.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.user1.id = :userId OR m.user2.id = :userId")
    List<Match> findAllByUserId(Long userId);
}