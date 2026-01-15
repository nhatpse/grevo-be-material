package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.entity.ServiceAreas;
import org.grevo.grevobematerial.repository.ServiceAreasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ServiceAreaService {

    private final ServiceAreasRepository serviceAreasRepository;
    private final LocationService locationService;
    private final org.grevo.grevobematerial.repository.EnterpriseAreaRepository enterpriseAreaRepository;

    public ServiceAreaService(ServiceAreasRepository serviceAreasRepository, LocationService locationService,
            org.grevo.grevobematerial.repository.EnterpriseAreaRepository enterpriseAreaRepository) {
        this.serviceAreasRepository = serviceAreasRepository;
        this.locationService = locationService;
        this.enterpriseAreaRepository = enterpriseAreaRepository;
    }

    @Transactional
    public ServiceAreas addSystemAreaFromQuery(String query) {
        Map<String, Object> locationData = locationService.forwardGeocode(query, null, null);
        if (locationData == null) {
            throw new RuntimeException("Could not find location for query: " + query);
        }

        String province = (String) locationData.get("province");
        if (province == null || province.isBlank()) {
            throw new RuntimeException("Could not determine province/city from query: " + query);
        }

        Double lat = (Double) locationData.get("lat");
        Double lng = (Double) locationData.get("lng");
        String type = "Province";

        return createSystemAreaDirectly(province, lat, lng, type);
    }

    @Transactional
    public ServiceAreas createSystemAreaDirectly(String name, Double lat, Double lng, String type) {
        java.util.Optional<ServiceAreas> existing = serviceAreasRepository.findByAreaName(name);
        if (existing.isPresent()) {
            ServiceAreas area = existing.get();
            if (!Boolean.TRUE.equals(area.getIsActive())) {
                area.setIsActive(true);
                return serviceAreasRepository.save(area);
            }
            return area;
        }

        ServiceAreas newArea = new ServiceAreas();
        newArea.setAreaName(name);
        newArea.setAreaCode(toSlug(name));
        newArea.setLat(lat);
        newArea.setLng(lng);
        newArea.setType(type);
        return serviceAreasRepository.save(newArea);
    }

    public List<ServiceAreas> getAllSystemAreas() {
        return serviceAreasRepository.findAllByIsActiveTrue();
    }

    @Transactional
    public void deleteSystemArea(Integer areaId) {
        ServiceAreas area = serviceAreasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Service Area not found with id: " + areaId));

        area.setIsActive(false);
        serviceAreasRepository.save(area);

        // Cascade soft delete to EnterpriseArea
        List<org.grevo.grevobematerial.entity.EnterpriseArea> enterpriseAreas = enterpriseAreaRepository
                .findByArea(area);
        for (org.grevo.grevobematerial.entity.EnterpriseArea ea : enterpriseAreas) {
            ea.setIsActive(false);
            enterpriseAreaRepository.save(ea);
        }
    }

    private String toSlug(String input) {
        if (input == null)
            return null;
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-|-$", "");
    }
}
