package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "ships")
public class Ship {

    @Id
    @GeneratedValue(generator = "ships_sequence")
    private Long id;

    private String type;

    private Integer length;

    private String fields;

    @Column(name = "is_destroyed")
    private Boolean isDestroyed = false;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "board_id")
    private Board board;

}
