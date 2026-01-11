package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Citizens;
import org.grevo.grevobematerial.entity.ServiceAreas;
import org.grevo.grevobematerial.entity.WasteReports;
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
}
