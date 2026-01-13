package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.response.AddressResponse;
import org.grevo.grevobematerial.service.geocoding.GeocodingProvider;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final GeocodingProvider geocodingProvider;

    public LocationService(GeocodingProvider geocodingProvider) {
        this.geocodingProvider = geocodingProvider;
    }

    public AddressResponse getAddressFromCoordinates(double latitude, double longitude) {
        return geocodingProvider.reverseGeocode(latitude, longitude);
    }
}
