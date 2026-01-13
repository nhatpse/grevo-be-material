package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.dto.response.AddressResponse;
import org.grevo.grevobematerial.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude) {

        try {
            AddressResponse address = locationService.getAddressFromCoordinates(latitude, longitude);
            if (address == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error resolving address: " + e.getMessage()));
        }
    }
}
