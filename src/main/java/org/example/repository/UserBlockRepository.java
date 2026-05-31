package org.example.repository;

import org.example.entities.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    Optional<UserBlock> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    @Query("SELECT b FROM UserBlock b JOIN FETCH b.blocked WHERE b.blocker.id = :blockerId ORDER BY b.blockedAt DESC")
    List<UserBlock> findByBlockerId(@Param("blockerId") Long blockerId);
}
