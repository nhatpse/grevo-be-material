package org.grevo.grevobematerial.controller;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.grevo.grevobematerial.service.WasteReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

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
}
