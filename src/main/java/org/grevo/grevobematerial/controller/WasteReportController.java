package org.grevo.grevobematerial.controller;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.EligibleCollectorResponse;
import org.grevo.grevobematerial.dto.response.WasteReportDetailResponse;
import org.grevo.grevobematerial.dto.response.WasteReportListResponse;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.grevo.grevobematerial.service.WasteReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class WasteReportController {

    private final WasteReportService wasteReportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WasteReportResponse> createReport(
            @ModelAttribute WasteReportRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Principal principal) {

        String userEmail = principal.getName();

        WasteReportResponse response = wasteReportService.createReport(request, images, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enterprise")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<Page<WasteReportListResponse>> getEnterpriseReports(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String userEmail = principal.getName();
        Page<WasteReportListResponse> reports = wasteReportService.getReportsForEnterprise(userEmail, pageable);

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/enterprise/history")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<Page<WasteReportListResponse>> getEnterpriseReportHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String userEmail = principal.getName();
        Page<WasteReportListResponse> reports = wasteReportService.getReportHistoryForEnterprise(userEmail, pageable);

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/enterprise/{reportId}")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<WasteReportDetailResponse> getReportDetail(
            @PathVariable Integer reportId,
            Principal principal) {
        WasteReportDetailResponse report = wasteReportService.getReportDetail(reportId, principal.getName());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/enterprise/{reportId}/eligible-collectors")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<List<EligibleCollectorResponse>> getEligibleCollectors(
            @PathVariable Integer reportId,
            Principal principal) {
        List<EligibleCollectorResponse> collectors = wasteReportService.getEligibleCollectors(reportId,
                principal.getName());
        return ResponseEntity.ok(collectors);
    }

    @PostMapping("/enterprise/{reportId}/assign")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<WasteReportDetailResponse> assignCollector(
            @PathVariable Integer reportId,
            @RequestBody Map<String, Integer> body,
            Principal principal) {
        Integer collectorId = body.get("collectorId");
        if (collectorId == null) {
            throw new RuntimeException("collectorId is required");
        }
        WasteReportDetailResponse report = wasteReportService.assignCollector(reportId, collectorId,
                principal.getName());
        return ResponseEntity.ok(report);
    }

    @PutMapping("/enterprise/{reportId}/status")
    @PreAuthorize("hasAuthority('ENTERPRISE')")
    public ResponseEntity<WasteReportDetailResponse> updateReportStatus(
            @PathVariable Integer reportId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String status = body.get("status");
        if (status == null) {
            throw new RuntimeException("status is required");
        }
        WasteReportDetailResponse report = wasteReportService.updateReportStatus(reportId, status, principal.getName());
        return ResponseEntity.ok(report);
    }
}
