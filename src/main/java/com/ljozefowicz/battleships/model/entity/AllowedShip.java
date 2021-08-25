package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "allowed_ships")
public class AllowedShip {

    @Id
    private Long id;

    private String type;

    private int length;

    @Column(name = "num_allowed")
    private int numberOfAllowed;

}
