package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entities.UserReport;
import org.example.repository.UserDataRepository;
import org.example.repository.UserReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserReportRepository reportRepository;
    private final UserDataRepository userDataRepository;

    @Transactional
    public void reportUser(Long reporterId, Long reportedId, String reason) {
        if (reporterId.equals(reportedId)) {
            throw new IllegalArgumentException("Cannot report yourself");
        }
        if (reportRepository.existsByReporter_IdAndReported_Id(reporterId, reportedId)) {
            throw new IllegalStateException("Already reported this user");
        }
        UserReport report = UserReport.builder()
                .reporter(userDataRepository.getReferenceById(reporterId))
                .reported(userDataRepository.getReferenceById(reportedId))
                .reason(reason)
                .build();
        reportRepository.save(report);
    }
}