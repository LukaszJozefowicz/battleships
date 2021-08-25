package com.ljozefowicz.battleships.model.entity;

import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.enums.GameTurn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Game {

    @Id
    @GeneratedValue(generator = "game_info_sequence")
    private Long id;

    @ManyToOne
    private User player1;

    @ManyToOne
    private User player2;

    @OneToOne
    @JoinColumn(name="player1_board")
    private Board firstPlayerBoard;

    @OneToOne
    @JoinColumn(name="player2_board")
    private Board secondPlayerBoard;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_state", nullable = false)
    private GameState gameState;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_turn", nullable = false)
    private GameTurn playerTurn;
}
