package org.grevo.grevobematerial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waste_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WasteTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId;

    @Column(nullable = false)
    private String name;

    private Integer basePoints;
}
