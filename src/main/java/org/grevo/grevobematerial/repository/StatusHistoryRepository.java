package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.StatusHistory;
import org.grevo.grevobematerial.entity.WasteReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Integer> {

    List<StatusHistory> findByReport(WasteReports report);

    List<StatusHistory> findByReportOrderByChangedAtDesc(WasteReports report);
}
