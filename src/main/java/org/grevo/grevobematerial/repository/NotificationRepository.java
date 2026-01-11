package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Notification;
import org.grevo.grevobematerial.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser(Users user);

    List<Notification> findByUserAndIsRead(Users user, Boolean isRead);

    long countByUserAndIsRead(Users user, Boolean isRead);
}
