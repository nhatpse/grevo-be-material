package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grevo.grevobematerial.entity.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "collector_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private org.grevo.grevobematerial.entity.enums.RequestType type = org.grevo.grevobematerial.entity.enums.RequestType.JOIN;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
