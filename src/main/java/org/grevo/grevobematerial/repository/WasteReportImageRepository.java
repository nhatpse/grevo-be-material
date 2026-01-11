package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.WasteReportImage;
import org.grevo.grevobematerial.entity.WasteReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportImageRepository extends JpaRepository<WasteReportImage, Integer> {

    List<WasteReportImage> findByReport(WasteReports report);
}
