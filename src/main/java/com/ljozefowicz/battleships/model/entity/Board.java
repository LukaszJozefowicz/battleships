package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ljozefowicz.battleships.enums.FieldStatus;
import lombok.*;
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

    @EqualsAndHashCode.Exclude  //doesn't matter if it's here or in Ship entity. Needed for saving PC ships to DB (bean)
    @JsonManagedReference
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    List<Ship> ships;
}


