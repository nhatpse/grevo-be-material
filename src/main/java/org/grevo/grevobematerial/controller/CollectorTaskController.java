package org.grevo.grevobematerial.controller;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.response.WasteReportDetailResponse;
import org.grevo.grevobematerial.dto.response.WasteReportListResponse;
import org.grevo.grevobematerial.entity.*;
import org.grevo.grevobematerial.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collector/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('COLLECTOR')")
public class CollectorTaskController {

    private final UserRepository userRepository;
    private final CollectorsRepository collectorsRepository;
    private final WasteReportsRepository wasteReportsRepository;
    private final WasteReportImageRepository wasteReportImageRepository;

    /**
     * Get tasks assigned to the current collector
     */
    @GetMapping
    public ResponseEntity<List<WasteReportListResponse>> getMyTasks(Principal principal) {
        Collectors collector = getCollector(principal.getName());

        List<String> activeStatuses = List.of("ASSIGNED", "ON_THE_WAY");
        List<WasteReports> reports = wasteReportsRepository.findByAssignedCollectorAndStatusIn(collector,
                activeStatuses);

        List<WasteReportListResponse> response = reports.stream()
                .map(this::mapToListResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get task detail
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<WasteReportDetailResponse> getTaskDetail(
            @PathVariable Integer reportId,
            Principal principal) {
        Collectors collector = getCollector(principal.getName());
        WasteReports report = getReportForCollector(reportId, collector);

        List<String> imageUrls = wasteReportImageRepository.findByReport(report)
                .stream().map(WasteReportImage::getImageUrl).toList();

        return ResponseEntity.ok(mapToDetailResponse(report, imageUrls));
    }

    /**
     * Accept task - status changes to ON_THE_WAY
     */
    @PostMapping("/{reportId}/accept")
    public ResponseEntity<?> acceptTask(
            @PathVariable Integer reportId,
            Principal principal) {
        Collectors collector = getCollector(principal.getName());
        WasteReports report = getReportForCollector(reportId, collector);

        if (!"ASSIGNED".equals(report.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task is not in ASSIGNED status"));
        }

        report.setStatus("ON_THE_WAY");
        wasteReportsRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Task accepted successfully"));
    }

    /**
     * Reject task - status changes back to PENDING, collector unassigned
     */
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<?> rejectTask(
            @PathVariable Integer reportId,
            Principal principal) {
        Collectors collector = getCollector(principal.getName());
        WasteReports report = getReportForCollector(reportId, collector);

        if (!"ASSIGNED".equals(report.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task is not in ASSIGNED status"));
        }

        report.setStatus("PENDING");
        report.setAssignedCollector(null);
        report.setAssignedAt(null);
        wasteReportsRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Task rejected, returned to enterprise"));
    }

    /**
     * Complete task - status changes to COLLECTED
     */
    @PostMapping("/{reportId}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable Integer reportId,
            Principal principal) {
        Collectors collector = getCollector(principal.getName());
        WasteReports report = getReportForCollector(reportId, collector);

        if (!"ON_THE_WAY".equals(report.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task is not in ON_THE_WAY status"));
        }

        report.setStatus("COLLECTED");
        wasteReportsRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Task completed successfully"));
    }

    /**
     * Cancel task - status changes to CANCELLED (collector unable to complete)
     */
    @PostMapping("/{reportId}/cancel")
    public ResponseEntity<?> cancelTask(
            @PathVariable Integer reportId,
            Principal principal) {
        Collectors collector = getCollector(principal.getName());
        WasteReports report = getReportForCollector(reportId, collector);

        if (!"ON_THE_WAY".equals(report.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task is not in ON_THE_WAY status"));
        }

        report.setStatus("CANCELLED");
        wasteReportsRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Task cancelled"));
    }

    // ===== Helper Methods =====

    private Collectors getCollector(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return collectorsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Collector not found for user: " + username));
    }

    private WasteReports getReportForCollector(Integer reportId, Collectors collector) {
        WasteReports report = wasteReportsRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
        if (report.getAssignedCollector() == null ||
                !report.getAssignedCollector().getCollectorId().equals(collector.getCollectorId())) {
            throw new RuntimeException("Report is not assigned to this collector");
        }
        return report;
    }

    private WasteReportListResponse mapToListResponse(WasteReports report) {
        return WasteReportListResponse.builder()
                .reportId(report.getReportId())
                .title(report.getTitle())
                .status(report.getStatus())
                .wasteType(report.getWasteType())
                .createdAt(report.getCreatedAt())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .qualityScore(report.getQualityScore())
                .citizenName(report.getCitizen().getUser().getFullName())
                .areaName(report.getArea() != null ? report.getArea().getAreaName() : null)
                .build();
    }

    private WasteReportDetailResponse mapToDetailResponse(WasteReports report, List<String> imageUrls) {
        Users citizen = report.getCitizen().getUser();

        return WasteReportDetailResponse.builder()
                .reportId(report.getReportId())
                .title(report.getTitle())
                .description(report.getDescription())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .status(report.getStatus())
                .wasteQuantity(report.getWasteQuantity())
                .wasteType(report.getWasteType())
                .createdAt(report.getCreatedAt())
                .assignedAt(report.getAssignedAt())
                .imageUrls(imageUrls)
                .citizenName(citizen.getFullName())
                .citizenEmail(citizen.getEmail())
                .citizenPhone(citizen.getPhone())
                .areaId(report.getArea() != null ? report.getArea().getAreaId() : null)
                .areaName(report.getArea() != null ? report.getArea().getAreaName() : null)
                .build();
    }
}
