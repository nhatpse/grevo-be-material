package org.grevo.grevobematerial.service.impl;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.grevo.grevobematerial.entity.*;
import org.grevo.grevobematerial.repository.*;
import org.grevo.grevobematerial.service.CloudinaryService;
import org.grevo.grevobematerial.service.WasteReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WasteReportServiceImpl implements WasteReportService {

    private final WasteReportsRepository wasteReportsRepository;
    private final WasteReportImageRepository wasteReportImageRepository;
    private final CitizensRepository citizensRepository;
    private final UserRepository userRepository;
    private final ServiceAreasRepository serviceAreasRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public WasteReportResponse createReport(WasteReportRequest request, List<MultipartFile> images, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Citizens citizen = citizensRepository.findByUser(user)
                .orElseGet(() -> {
                    Citizens newCitizen = new Citizens();
                    newCitizen.setUser(user);
                    newCitizen.setTotalPoints(0);
                    return citizensRepository.save(newCitizen);
                });

        WasteReports report = new WasteReports();
        report.setCitizen(citizen);
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setStatus("PENDING");
        report.setWasteQuantity(request.getWasteQuantity());
        report.setItemWeights(request.getItemWeights());

        report.setWasteType(request.getWasteType());

        if (request.getAreaId() != null) {
            ServiceAreas area = serviceAreasRepository.findById(request.getAreaId())
                    .orElse(null);
            report.setArea(area);
        }

        report = wasteReportsRepository.save(report);

        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                try {
                    String imageUrl = cloudinaryService.uploadImage(file, "grevo/reports/" + report.getReportId());

                    WasteReportImage reportImage = new WasteReportImage();
                    reportImage.setReport(report);
                    reportImage.setImageUrl(imageUrl);
                    reportImage.setImageType(file.getContentType());

                    wasteReportImageRepository.save(reportImage);
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image", e);
                }
            }
        }

        return WasteReportResponse.builder()
                .reportId(report.getReportId())
                .title(report.getTitle())
                .description(report.getDescription())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .status(report.getStatus())
                .qualityScore(report.getQualityScore())
                .createdAt(report.getCreatedAt())
                .wasteQuantity(report.getWasteQuantity())
                .itemWeights(report.getItemWeights())
                .wasteTypeName(report.getWasteType())
                .imageUrls(imageUrls)
                .build();
    }
}
