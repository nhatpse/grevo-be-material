package org.grevo.grevobematerial.service;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.EnterpriseArea;
import org.grevo.grevobematerial.entity.ServiceAreas;
import org.grevo.grevobematerial.repository.EnterpriseAreaRepository;
import org.grevo.grevobematerial.repository.EnterpriseRepository;
import org.grevo.grevobematerial.repository.ServiceAreasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EnterpriseAreaService {

    private final EnterpriseRepository enterpriseRepository;
    private final ServiceAreasRepository serviceAreasRepository;
    private final EnterpriseAreaRepository enterpriseAreaRepository;
    private final LocationService locationService;

    public EnterpriseAreaService(EnterpriseRepository enterpriseRepository,
            ServiceAreasRepository serviceAreasRepository,
            EnterpriseAreaRepository enterpriseAreaRepository,
            LocationService locationService) {
        this.enterpriseRepository = enterpriseRepository;
        this.serviceAreasRepository = serviceAreasRepository;
        this.enterpriseAreaRepository = enterpriseAreaRepository;
        this.locationService = locationService;
    }

    @Transactional
    public EnterpriseArea addArea(Integer enterpriseId, String query) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found with id: " + enterpriseId));

        Map<String, Object> locationData = locationService.forwardGeocode(query, null, null);
        if (locationData == null) {
            throw new RuntimeException("Could not find location for query: " + query);
        }

        String province = (String) locationData.get("province");
        if (province == null || province.isBlank()) {
            throw new RuntimeException("Could not determine province/city from query: " + query);
        }

        ServiceAreas area = serviceAreasRepository.findByAreaName(province)
                .orElseGet(() -> {
                    ServiceAreas newArea = new ServiceAreas();
                    newArea.setAreaName(province);
                    newArea.setAreaCode(toSlug(province));
                    return serviceAreasRepository.save(newArea);
                });

        Optional<EnterpriseArea> existing = enterpriseAreaRepository.findByEnterpriseAndArea(enterprise, area);
        if (existing.isPresent()) {
            EnterpriseArea ea = existing.get();
            if (!ea.getIsActive()) {
                ea.setIsActive(true);
                return enterpriseAreaRepository.save(ea);
            }
            return ea;
        }

        EnterpriseArea enterpriseArea = new EnterpriseArea();
        enterpriseArea.setEnterprise(enterprise);
        enterpriseArea.setArea(area);
        enterpriseArea.setIsActive(true);

        return enterpriseAreaRepository.save(enterpriseArea);
    }

    @Transactional
    public EnterpriseArea addAreaById(Integer enterpriseId, Integer areaId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found with id: " + enterpriseId));

        ServiceAreas area = serviceAreasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Service Area not found with id: " + areaId));

        Optional<EnterpriseArea> existing = enterpriseAreaRepository.findByEnterpriseAndArea(enterprise, area);
        if (existing.isPresent()) {
            EnterpriseArea ea = existing.get();
            if (!ea.getIsActive()) {
                ea.setIsActive(true);
                return enterpriseAreaRepository.save(ea);
            }
            return ea;
        }

        EnterpriseArea enterpriseArea = new EnterpriseArea();
        enterpriseArea.setEnterprise(enterprise);
        enterpriseArea.setArea(area);
        enterpriseArea.setIsActive(true);

        return enterpriseAreaRepository.save(enterpriseArea);
    }

    @Transactional
    public void removeArea(Integer enterpriseId, Integer areaId) {
        EnterpriseArea enterpriseArea = enterpriseAreaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Enterprise Area link not found"));

        if (!enterpriseArea.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("Area does not belong to this enterprise");
        }

        enterpriseArea.setIsActive(false);
        enterpriseAreaRepository.delete(enterpriseArea);
    }

    public List<EnterpriseArea> getAreas(Integer enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));
        return enterpriseAreaRepository.findByEnterpriseAndIsActive(enterprise, true);
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
