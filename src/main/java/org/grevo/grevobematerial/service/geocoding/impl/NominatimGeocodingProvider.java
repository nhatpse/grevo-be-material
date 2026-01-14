package org.grevo.grevobematerial.service.geocoding.impl;

import org.grevo.grevobematerial.dto.request.CoordinatesRequest;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.grevo.grevobematerial.service.geocoding.GeocodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Geocoding provider that uses OpenMap.vn as the sole provider.
 */
@Service
public class NominatimGeocodingProvider implements GeocodingProvider {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodingProvider.class);
    private final OpenMapGeocodingProvider openMapProvider;

    public NominatimGeocodingProvider(OpenMapGeocodingProvider openMapProvider) {
        this.openMapProvider = openMapProvider;
    }

    @Override
    public LocationResponse reverseGeocode(CoordinatesRequest request) {
        try {
            double latitude = request.getLat();
            double longitude = request.getLng();

            LocationResponse result = openMapProvider.reverseGeocode(latitude, longitude);
            if (result != null) {
                return result;
            }

            log.warn("OpenMap.vn reverse geocode returned no result for {}, {}", latitude, longitude);
            return null;

        } catch (Exception e) {
            log.error("Geocoding failed: {}", e.getMessage());
            return null;
        }
    }
}
