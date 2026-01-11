package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "collectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collectors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer collectorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    private String vehicleType;

    private String vehiclePlate;

    private Integer maxCapacity;

    private String currentStatus;

    private LocalDateTime lastActiveAt;
}
