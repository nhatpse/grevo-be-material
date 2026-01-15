package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.ServiceAreas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceAreasRepository extends JpaRepository<ServiceAreas, Integer> {

    Optional<ServiceAreas> findByAreaCode(String areaCode);

    Optional<ServiceAreas> findByAreaName(String areaName);

    java.util.List<ServiceAreas> findAllByIsActiveTrue();

    Optional<ServiceAreas> findFirstByAreaNameContainingIgnoreCaseAndIsActiveTrue(String areaName);
}
