package com.ljozefowicz.battleships.dto;

import com.ljozefowicz.battleships.enums.GameState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameDto {
    private Long id;
    private String player1;
    private String player2;
    private String gameState;
}
