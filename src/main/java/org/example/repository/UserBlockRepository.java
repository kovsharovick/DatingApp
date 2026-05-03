package org.example.repository;

import org.example.entities.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
}