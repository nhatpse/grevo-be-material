package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.EnterpriseArea;
import org.grevo.grevobematerial.entity.ServiceAreas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterpriseAreaRepository extends JpaRepository<EnterpriseArea, Integer> {

    List<EnterpriseArea> findByEnterprise(Enterprise enterprise);

    List<EnterpriseArea> findByArea(ServiceAreas area);

    List<EnterpriseArea> findByEnterpriseAndIsActive(Enterprise enterprise, Boolean isActive);

    java.util.Optional<EnterpriseArea> findByEnterpriseAndArea(Enterprise enterprise, ServiceAreas area);
}
