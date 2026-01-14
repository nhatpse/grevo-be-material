package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.dto.request.CoordinatesRequest;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.grevo.grevobematerial.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Location controller providing geocoding and place search endpoints.
 * Uses Goong.io APIs for Vietnamese location data.
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Reverse geocode coordinates to address.
     * POST /api/location
     */
    @PostMapping
    public ResponseEntity<?> reverseGeocode(@RequestBody CoordinatesRequest request) {
        try {
            LocationResponse location = locationService.getAddressFromCoordinates(request);
            if (location == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error resolving address: " + e.getMessage()));
        }
    }

    /**
     * Autocomplete search for places.
     * GET /api/location/autocomplete?text=...&session_token=...
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String text,
            @RequestParam(name = "session_token", required = false) String sessionToken) {
        String result = locationService.autocomplete(text, sessionToken);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Autocomplete failed or API key invalid"));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get place details by place_id.
     * GET /api/location/details?ids=...&session_token=...
     */
    @GetMapping("/details")
    public ResponseEntity<?> getPlaceDetails(@RequestParam String ids,
            @RequestParam(name = "session_token", required = false) String sessionToken) {
        String result = locationService.getPlaceDetail(ids, sessionToken);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Generate static map URL for a location.
     * GET /api/location/static-map?lat=...&lng=...
     */
    @GetMapping("/static-map")
    public ResponseEntity<?> getStaticMap(@RequestParam double lat, @RequestParam double lng) {
        String url = locationService.getStaticMapUrl(lat, lng);
        if (url == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Could not generate map URL"));
        }
        return ResponseEntity.ok(Map.of("url", url));
    }
}
