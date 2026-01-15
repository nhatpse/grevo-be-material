package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteReportDetailResponse {
    private Integer reportId;
    private String title;
    private String description;
    private String latitude;
    private String longitude;
    private String status;
    private Double wasteQuantity;
    private String wasteType;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private List<String> imageUrls;

    private String citizenName;
    private String citizenEmail;
    private String citizenPhone;

    private Integer areaId;
    private String areaName;

    private Integer assignedCollectorId;
    private String assignedCollectorName;
    private String assignedCollectorPhone;
    private String assignedCollectorVehicleType;
    private String assignedCollectorVehiclePlate;
}
