package org.grevo.grevobematerial.dto.request;

import lombok.Data;

@Data
public class CollectorProfileRequest {
    private String vehicleType;
    private String vehiclePlate;
    private Integer maxCapacity;
}
