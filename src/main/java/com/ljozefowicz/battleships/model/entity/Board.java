package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ljozefowicz.battleships.enums.FieldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(generator = "boards_sequence")
    private Long id;

    @Column(name = "persisted_board", columnDefinition = "text")
    private String persistedBoard;

    @JsonManagedReference
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    List<Ship> ships;
}


