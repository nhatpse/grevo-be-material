package org.grevo.grevobematerial.service.impl;

import lombok.RequiredArgsConstructor;
import org.grevo.grevobematerial.dto.request.WasteReportRequest;
import org.grevo.grevobematerial.dto.response.EligibleCollectorResponse;
import org.grevo.grevobematerial.dto.response.WasteReportDetailResponse;
import org.grevo.grevobematerial.dto.response.WasteReportListResponse;
import org.grevo.grevobematerial.dto.response.WasteReportResponse;
import org.grevo.grevobematerial.entity.*;
import org.grevo.grevobematerial.repository.*;
import org.grevo.grevobematerial.service.CloudinaryService;
import org.grevo.grevobematerial.service.WasteReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseAreaRepository enterpriseAreaRepository;
    private final CollectorsRepository collectorsRepository;

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
        } else if (request.getProvince() != null && !request.getProvince().isBlank()) {
            serviceAreasRepository
                    .findFirstByAreaNameContainingIgnoreCaseAndIsActiveTrue(request.getProvince())
                    .ifPresent(report::setArea);
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

    @Override
    @Transactional(readOnly = true)
    public Page<WasteReportListResponse> getReportsForEnterprise(String userEmail, Pageable pageable) {
        List<Integer> areaIds = getEnterpriseAreaIds(userEmail);
        if (areaIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> activeStatuses = List.of("PENDING", "ASSIGNED", "ON_THE_WAY");
        Page<WasteReports> reportsPage = wasteReportsRepository.findByArea_AreaIdInAndStatusIn(areaIds, activeStatuses,
                pageable);

        return reportsPage.map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WasteReportListResponse> getReportHistoryForEnterprise(String userEmail, Pageable pageable) {
        List<Integer> areaIds = getEnterpriseAreaIds(userEmail);
        if (areaIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> historyStatuses = List.of("COLLECTED", "CANCELLED");
        Page<WasteReports> reportsPage = wasteReportsRepository.findByArea_AreaIdInAndStatusIn(areaIds, historyStatuses,
                pageable);

        return reportsPage.map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public WasteReportDetailResponse getReportDetail(Integer reportId, String enterpriseEmail) {
        WasteReports report = getReportForEnterprise(reportId, enterpriseEmail);
        List<String> imageUrls = wasteReportImageRepository.findByReport(report)
                .stream().map(WasteReportImage::getImageUrl).toList();
        return mapToDetailResponse(report, imageUrls);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleCollectorResponse> getEligibleCollectors(Integer reportId, String enterpriseEmail) {
        WasteReports report = getReportForEnterprise(reportId, enterpriseEmail);
        Enterprise enterprise = getEnterprise(enterpriseEmail);

        List<org.grevo.grevobematerial.entity.Collectors> collectors = collectorsRepository
                .findByEnterprise(enterprise);

        Double reportWeight = report.getWasteQuantity() != null ? report.getWasteQuantity() : 0.0;

        return collectors.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsOnline()))
                .filter(c -> c.getMaxCapacity() != null && c.getMaxCapacity() >= reportWeight)
                .map(c -> {
                    Users u = c.getUser();
                    return EligibleCollectorResponse.builder()
                            .collectorId(c.getCollectorId())
                            .userId(u.getUserId())
                            .fullName(u.getFullName())
                            .email(u.getEmail())
                            .phone(u.getPhone())
                            .vehicleType(c.getVehicleType())
                            .vehiclePlate(c.getVehiclePlate())
                            .maxCapacity(c.getMaxCapacity())
                            .distanceKm(null) // Distance not available
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public WasteReportDetailResponse assignCollector(Integer reportId, Integer collectorId, String enterpriseEmail) {
        WasteReports report = getReportForEnterprise(reportId, enterpriseEmail);
        Enterprise enterprise = getEnterprise(enterpriseEmail);

        org.grevo.grevobematerial.entity.Collectors collector = collectorsRepository.findById(collectorId)
                .orElseThrow(() -> new RuntimeException("Collector not found: " + collectorId));

        if (collector.getEnterprise() == null
                || !collector.getEnterprise().getEnterpriseId().equals(enterprise.getEnterpriseId())) {
            throw new RuntimeException("Collector does not belong to this enterprise");
        }

        if (!Boolean.TRUE.equals(collector.getIsOnline())) {
            throw new RuntimeException("Collector is currently OFFLINE and cannot be assigned tasks");
        }

        report.setAssignedCollector(collector);
        report.setAssignedAt(LocalDateTime.now());
        report.setStatus("ASSIGNED");
        wasteReportsRepository.save(report);

        List<String> imageUrls = wasteReportImageRepository.findByReport(report)
                .stream().map(WasteReportImage::getImageUrl).toList();
        return mapToDetailResponse(report, imageUrls);
    }

    @Override
    @Transactional
    public WasteReportDetailResponse updateReportStatus(Integer reportId, String status, String enterpriseEmail) {
        WasteReports report = getReportForEnterprise(reportId, enterpriseEmail);

        List<String> validStatuses = List.of("PENDING", "ASSIGNED", "ON_THE_WAY", "COLLECTED", "CANCELLED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new RuntimeException("Invalid status: " + status);
        }

        report.setStatus(status.toUpperCase());
        wasteReportsRepository.save(report);

        List<String> imageUrls = wasteReportImageRepository.findByReport(report)
                .stream().map(WasteReportImage::getImageUrl).toList();
        return mapToDetailResponse(report, imageUrls);
    }


    private List<Integer> getEnterpriseAreaIds(String userEmail) {
        Users user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        Enterprise enterprise = enterpriseRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Enterprise not found for user: " + userEmail));
        List<EnterpriseArea> enterpriseAreas = enterpriseAreaRepository.findByEnterpriseAndIsActive(enterprise, true);
        return enterpriseAreas.stream().map(ea -> ea.getArea().getAreaId()).toList();
    }

    private Enterprise getEnterprise(String userEmail) {
        Users user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        return enterpriseRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Enterprise not found for user: " + userEmail));
    }

    private WasteReports getReportForEnterprise(Integer reportId, String enterpriseEmail) {
        WasteReports report = wasteReportsRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
        List<Integer> areaIds = getEnterpriseAreaIds(enterpriseEmail);
        if (report.getArea() == null || !areaIds.contains(report.getArea().getAreaId())) {
            throw new RuntimeException("Report does not belong to enterprise's areas");
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
        org.grevo.grevobematerial.entity.Collectors collector = report.getAssignedCollector();

        WasteReportDetailResponse.WasteReportDetailResponseBuilder builder = WasteReportDetailResponse.builder()
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
                .areaName(report.getArea() != null ? report.getArea().getAreaName() : null);

        if (collector != null) {
            Users collectorUser = collector.getUser();
            builder.assignedCollectorId(collector.getCollectorId())
                    .assignedCollectorName(collectorUser.getFullName())
                    .assignedCollectorPhone(collectorUser.getPhone())
                    .assignedCollectorVehicleType(collector.getVehicleType())
                    .assignedCollectorVehiclePlate(collector.getVehiclePlate());
        }

        return builder.build();
    }

    private double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double calculateDistance(double lat1, double lng1, Double lat2, Double lng2) {
        if (lat2 == null || lng2 == null)
            return 9999.0;
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
