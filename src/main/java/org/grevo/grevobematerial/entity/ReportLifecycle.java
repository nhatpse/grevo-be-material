package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_lifecycle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportLifecycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lifecycleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private Collectors collector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private WasteReports report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    private LocalDateTime acceptedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime collectedAt;
}
