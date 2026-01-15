package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.EligibleCollectorResponse;
import org.grevo.grevobematerial.dto.response.WasteReportDetailResponse;
import org.grevo.grevobematerial.dto.response.WasteReportListResponse;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WasteReportService {
    WasteReportResponse createReport(WasteReportRequest request, List<MultipartFile> images, String username);

    Page<WasteReportListResponse> getReportsForEnterprise(String userEmail, Pageable pageable);

    Page<WasteReportListResponse> getReportHistoryForEnterprise(String userEmail, Pageable pageable);

    WasteReportDetailResponse getReportDetail(Integer reportId, String enterpriseEmail);

    List<EligibleCollectorResponse> getEligibleCollectors(Integer reportId, String enterpriseEmail);

    WasteReportDetailResponse assignCollector(Integer reportId, Integer collectorId, String enterpriseEmail);

    WasteReportDetailResponse updateReportStatus(Integer reportId, String status, String enterpriseEmail);
}
