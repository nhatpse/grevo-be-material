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
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private Integer areaId;

    @com.fasterxml.jackson.annotation.JsonProperty("name")
    private String areaName;

    private String areaCode;

    private String type;

    private Double lat;

    private Double lng;

    private Boolean isActive = true;
}
