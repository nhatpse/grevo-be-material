package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteReportListResponse {
    private Integer reportId;
    private String title;
    private String status;
    private String wasteType;
    private LocalDateTime createdAt;
    private String latitude;
    private String longitude;
    private Integer qualityScore;
    private String citizenName;
    private String areaName;
}
