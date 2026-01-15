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

        java.util.List<CollectorRequest> findAllByUser(Users user);

        boolean existsByUserAndStatus(Users user, RequestStatus status);

        Optional<CollectorRequest> findByUser(Users user);

        Optional<CollectorRequest> findByUserAndStatusAndType(Users user, RequestStatus status,
                        org.grevo.grevobematerial.entity.enums.RequestType type);

        java.util.List<CollectorRequest> findByEnterpriseAndStatusAndType(
                        org.grevo.grevobematerial.entity.Enterprise enterprise, RequestStatus status,
                        org.grevo.grevobematerial.entity.enums.RequestType type);

        boolean existsByUserAndStatusAndType(Users user, RequestStatus status,
                        org.grevo.grevobematerial.entity.enums.RequestType type);

        // Legacy Support methods

        java.util.List<CollectorRequest> findByEnterpriseAndStatus(
                        org.grevo.grevobematerial.entity.Enterprise enterprise, RequestStatus status);
}
