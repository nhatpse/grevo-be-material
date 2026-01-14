package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.request.CoordinatesRequest;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.grevo.grevobematerial.service.geocoding.GoongGeocodingProvider;
import org.springframework.stereotype.Service;

/**
 * Location service using Goong.io APIs for geocoding and place search.
 */
@Service
public class LocationService {

    private final GoongGeocodingProvider goongProvider;

    public LocationService(GoongGeocodingProvider goongProvider) {
        this.goongProvider = goongProvider;
    }

    /**
     * Reverse geocode coordinates to address.
     */
    public LocationResponse getAddressFromCoordinates(CoordinatesRequest request) {
        return goongProvider.reverseGeocode(request.getLat(), request.getLng());
    }

    /**
     * Autocomplete search for places.
     */
    public String autocomplete(String text, String sessionToken) {
        return goongProvider.autocomplete(text, sessionToken);
    }

    /**
     * Get place details by place_id.
     */
    public String getPlaceDetail(String placeId, String sessionToken) {
        return goongProvider.getPlaceDetail(placeId, sessionToken);
    }

    /**
     * Generate static map URL for a location.
     */
    public String getStaticMapUrl(double lat, double lng) {
        return goongProvider.getStaticMapUrl(lat, lng);
    }
}
