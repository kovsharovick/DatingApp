package org.example.repository;

import org.example.entities.UserVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVideoRepository extends JpaRepository<UserVideo, Long> {
    List<UserVideo> findByUserId(Long userId);
}