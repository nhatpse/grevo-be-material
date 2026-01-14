package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.grevo.grevobematerial.service.geocoding.GeocodingProvider;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final GeocodingProvider geocodingProvider;
    private final org.grevo.grevobematerial.service.geocoding.impl.OpenMapGeocodingProvider openMapProvider;

    public LocationService(GeocodingProvider geocodingProvider,
            org.grevo.grevobematerial.service.geocoding.impl.OpenMapGeocodingProvider openMapProvider) {
        this.geocodingProvider = geocodingProvider;
        this.openMapProvider = openMapProvider;
    }

    public LocationResponse getAddressFromCoordinates(
            org.grevo.grevobematerial.dto.request.CoordinatesRequest request) {
        return geocodingProvider.reverseGeocode(request);
    }

    public String autocomplete(String text, String sessionToken) {
        return openMapProvider.autocomplete(text, sessionToken);
    }

    public String getPlaceDetail(String ids, String sessionToken) {
        return openMapProvider.getPlaceDetail(ids, sessionToken);
    }

    public String getStaticMapUrl(double lat, double lng) {
        return openMapProvider.getStaticMapUrl(lat, lng);
    }
}
