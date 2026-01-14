package org.grevo.grevobematerial.service.geocoding;

import org.grevo.grevobematerial.dto.request.CoordinatesRequest;
import org.grevo.grevobematerial.dto.response.LocationResponse;

public interface GeocodingProvider {
    LocationResponse reverseGeocode(CoordinatesRequest request);
}
