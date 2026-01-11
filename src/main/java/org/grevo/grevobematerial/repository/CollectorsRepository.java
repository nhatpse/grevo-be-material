package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Collectors;
import org.grevo.grevobematerial.entity.Enterprise;
import org.grevo.grevobematerial.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorsRepository extends JpaRepository<Collectors, Integer> {

    Optional<Collectors> findByUser(Users user);

    List<Collectors> findByEnterprise(Enterprise enterprise);

    List<Collectors> findByCurrentStatus(String currentStatus);
}
