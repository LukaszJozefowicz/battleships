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

    private int length;

    private String fields;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "board_id")
    private Board board;

}
