package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAreas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer areaId;

    private String areaName;

    private String areaCode;
}
