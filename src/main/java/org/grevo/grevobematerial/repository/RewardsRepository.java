package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Citizens;
import org.grevo.grevobematerial.entity.Rewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardsRepository extends JpaRepository<Rewards, Integer> {

    List<Rewards> findByCitizen(Citizens citizen);

    List<Rewards> findByCitizenOrderByCreatedAtDesc(Citizens citizen);

    @Query("SELECT SUM(r.points) FROM Rewards r WHERE r.citizen = ?1")
    Integer getTotalPointsByCitizen(Citizens citizen);
}
