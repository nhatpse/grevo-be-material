package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.CollectorRequest;
import org.grevo.grevobematerial.entity.Users;
import org.grevo.grevobematerial.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectorRequestRepository extends JpaRepository<CollectorRequest, Long> {
    Optional<CollectorRequest> findByUserAndStatus(Users user, RequestStatus status);

    boolean existsByUserAndStatus(Users user, RequestStatus status);

    java.util.List<CollectorRequest> findByEnterpriseAndStatus(org.grevo.grevobematerial.entity.Enterprise enterprise,
            RequestStatus status);
}
