package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.dto.request.CoordinatesRequest;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.grevo.grevobematerial.service.geocoding.GoongGeocodingProvider;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final GoongGeocodingProvider goongProvider;

    public LocationService(GoongGeocodingProvider goongProvider) {
        this.goongProvider = goongProvider;
    }

    public LocationResponse getAddressFromCoordinates(CoordinatesRequest request) {
        return goongProvider.reverseGeocode(request.getLat(), request.getLng());
    }

    public String autocomplete(String text, String sessionToken) {
        return goongProvider.autocomplete(text, sessionToken);
    }

    public String getPlaceDetail(String placeId, String sessionToken) {
        return goongProvider.getPlaceDetail(placeId, sessionToken);
    }

    public java.util.Map<String, Object> forwardGeocode(String address, Double lat, Double lng) {
        return goongProvider.forwardGeocode(address, lat, lng);
    }

    public String getStaticMapUrl(double lat, double lng) {
        return goongProvider.getStaticMapUrl(lat, lng);
    }
}
