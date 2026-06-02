package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.model.ReportRequest;
import org.example.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Жалобы на пользователей")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> report(
            Principal principal,
            @Valid @RequestBody ReportRequest request) {
        Long userId = Long.parseLong(principal.getName());
        reportService.reportUser(userId, request.getReportedUserId(), request.getReason());
        return ResponseEntity.noContent().build();
    }
}