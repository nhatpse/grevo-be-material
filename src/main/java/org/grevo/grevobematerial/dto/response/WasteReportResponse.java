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
public class WasteReportResponse {
    private Integer reportId;
    private String title;
    private String description;
    private String latitude;
    private String longitude;
    private String status;
    private Integer qualityScore;
    private LocalDateTime createdAt;
    private Double wasteQuantity;
    private String wasteTypeName;
    private List<String> imageUrls;
}
