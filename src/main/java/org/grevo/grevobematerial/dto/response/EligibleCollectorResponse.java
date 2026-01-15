package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibleCollectorResponse {
    private Integer collectorId;
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String vehicleType;
    private String vehiclePlate;
    private Integer maxCapacity;
    private Double distanceKm;
}
