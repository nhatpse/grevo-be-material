package org.grevo.grevobematerial.controller;

import org.grevo.grevobematerial.entity.EnterpriseArea;
import org.grevo.grevobematerial.service.EnterpriseAreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/enterprises")
public class AdminEnterpriseAreaController {

    private final EnterpriseAreaService enterpriseAreaService;

    public AdminEnterpriseAreaController(EnterpriseAreaService enterpriseAreaService) {
        this.enterpriseAreaService = enterpriseAreaService;
    }

    @PostMapping("/{enterpriseId}/areas")
    public ResponseEntity<?> addArea(@PathVariable Integer enterpriseId, @RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Query parameters is missing (query needed)"));
        }

        try {
            EnterpriseArea area = enterpriseAreaService.addArea(enterpriseId, query);
            return ResponseEntity.ok(area);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{enterpriseId}/areas/{areaId}")
    public ResponseEntity<?> removeArea(@PathVariable Integer enterpriseId, @PathVariable Integer areaId) {
        try {
            enterpriseAreaService.removeArea(enterpriseId, areaId);
            return ResponseEntity.ok(Map.of("message", "Area removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{enterpriseId}/areas")
    public ResponseEntity<List<EnterpriseArea>> getAreas(@PathVariable Integer enterpriseId) {
        return ResponseEntity.ok(enterpriseAreaService.getAreas(enterpriseId));
    }
}
