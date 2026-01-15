package org.grevo.grevobematerial.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteReportRequest {
    private String title;
    private String description;
    private String latitude;
    private String longitude;
    private String wasteType;
    private Integer areaId;
    private Double wasteQuantity;
    private String itemWeights;
    private String province;
}
