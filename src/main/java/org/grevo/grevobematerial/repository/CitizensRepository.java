package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Citizens;
import org.grevo.grevobematerial.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizensRepository extends JpaRepository<Citizens, Integer> {

    Optional<Citizens> findByUser(Users user);

    Optional<Citizens> findByUserUserId(Integer userId);
}
