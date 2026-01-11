package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Citizens;
import org.grevo.grevobematerial.entity.Feedback;
import org.grevo.grevobematerial.entity.WasteReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    List<Feedback> findByCitizen(Citizens citizen);

    List<Feedback> findByReport(WasteReports report);

    Optional<Feedback> findByCitizenAndReport(Citizens citizen, WasteReports report);
}
