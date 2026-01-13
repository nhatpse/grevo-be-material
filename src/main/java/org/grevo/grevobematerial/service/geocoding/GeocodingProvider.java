package org.grevo.grevobematerial.service.geocoding;

import org.grevo.grevobematerial.dto.response.AddressResponse;

public interface GeocodingProvider {
    AddressResponse reverseGeocode(double latitude, double longitude);
}
