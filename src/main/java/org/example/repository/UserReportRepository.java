package org.example.repository;

import org.example.entities.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    boolean existsByReporter_IdAndReported_Id(Long reporterId, Long reportedId);
}