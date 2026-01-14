package org.grevo.grevobematerial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponse {
    private String addressDetails; // Street name, house number, POI details
    private String city; // City / Province
    private String ward; // Ward / Commune
}
