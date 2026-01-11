package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizens citizen;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String latitude;

    private String longitude;

    private String status;

    private Integer qualityScore;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_type_id")
    private WasteTypes wasteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private ServiceAreas area;

    private Double wasteQuantity;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
