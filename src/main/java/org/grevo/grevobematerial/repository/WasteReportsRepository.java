package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Citizens;
import org.grevo.grevobematerial.entity.Collectors;
import org.grevo.grevobematerial.entity.ServiceAreas;
import org.grevo.grevobematerial.entity.WasteReports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportsRepository extends JpaRepository<WasteReports, Integer> {

    List<WasteReports> findByCitizen(Citizens citizen);

    List<WasteReports> findByStatus(String status);

    List<WasteReports> findByArea(ServiceAreas area);

    List<WasteReports> findByCitizenOrderByCreatedAtDesc(Citizens citizen);

    List<WasteReports> findAllByOrderByCreatedAtDesc();

    Page<WasteReports> findByArea_AreaIdIn(List<Integer> areaIds, Pageable pageable);

    Page<WasteReports> findByArea_AreaIdInAndStatusIn(List<Integer> areaIds, List<String> statuses, Pageable pageable);

    List<WasteReports> findByAssignedCollectorAndStatusIn(Collectors collector, List<String> statuses);
}
