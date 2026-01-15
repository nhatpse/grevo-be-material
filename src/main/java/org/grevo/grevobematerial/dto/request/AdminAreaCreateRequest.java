package org.grevo.grevobematerial.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaCreateRequest {
    private String name;
    private Double lat;
    private Double lng;
    private String type;
    private String query;
}
