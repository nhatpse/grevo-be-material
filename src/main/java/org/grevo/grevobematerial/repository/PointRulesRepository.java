package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.PointRules;
import org.grevo.grevobematerial.entity.WasteTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRulesRepository extends JpaRepository<PointRules, Integer> {

    List<PointRules> findByWasteType(WasteTypes wasteType);

    List<PointRules> findByEnterprise(Enterprise enterprise);

    Optional<PointRules> findByWasteTypeAndEnterprise(WasteTypes wasteType, Enterprise enterprise);
}
