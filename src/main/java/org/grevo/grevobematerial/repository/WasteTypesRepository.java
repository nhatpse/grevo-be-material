package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.WasteTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WasteTypesRepository extends JpaRepository<WasteTypes, Integer> {

    Optional<WasteTypes> findByName(String name);
}
