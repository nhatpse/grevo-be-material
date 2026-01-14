package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WasteReportService {
    WasteReportResponse createReport(WasteReportRequest request, List<MultipartFile> images, String userEmail);
}
