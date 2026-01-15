package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.ServiceAreas;
import org.grevo.grevobematerial.service.ServiceAreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/areas")
public class AdminServiceAreaController {

    private final ServiceAreaService serviceAreaService;

    public AdminServiceAreaController(ServiceAreaService serviceAreaService) {
        this.serviceAreaService = serviceAreaService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceAreas>> getAllAreas() {
        return ResponseEntity.ok(serviceAreaService.getAllSystemAreas());
    }

    @PostMapping
    public ResponseEntity<?> addArea(@RequestBody org.grevo.grevobematerial.dto.request.AdminAreaCreateRequest body) {
        String name = body.getName();

        if (name != null && !name.isBlank()) {
            Double lat = body.getLat();
            Double lng = body.getLng();
            String type = body.getType();

            try {
                ServiceAreas area = serviceAreaService.createSystemAreaDirectly(name, lat, lng, type);
                return ResponseEntity.ok(area);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }

        String query = body.getQuery();
        if (query != null && !query.isBlank()) {
            try {
                ServiceAreas area = serviceAreaService.addSystemAreaFromQuery(query);
                return ResponseEntity.ok(area);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", "Request must contain either 'name' (plus lat/lng/type) OR 'query'."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArea(@PathVariable Integer id) {
        try {
            serviceAreaService.deleteSystemArea(id);
            return ResponseEntity.ok(Map.of("message", "Service Area deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
