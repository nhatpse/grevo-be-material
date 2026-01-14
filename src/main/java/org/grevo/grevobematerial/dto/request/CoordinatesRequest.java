package org.grevo.grevobematerial.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesRequest {
    private double lat;
    private double lng;
    private Double accuracy; 
}
