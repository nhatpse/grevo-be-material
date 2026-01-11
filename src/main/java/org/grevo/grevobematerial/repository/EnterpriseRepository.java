package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Integer> {

    Optional<Enterprise> findByUser(Users user);

    Optional<Enterprise> findByTaxCode(String taxCode);
}
