package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Collectors;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.ReportLifecycle;
import org.grevo.grevobematerial.entity.WasteReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportLifecycleRepository extends JpaRepository<ReportLifecycle, Integer> {

    Optional<ReportLifecycle> findByReport(WasteReports report);

    List<ReportLifecycle> findByCollector(Collectors collector);

    List<ReportLifecycle> findByEnterprise(Enterprise enterprise);
}
