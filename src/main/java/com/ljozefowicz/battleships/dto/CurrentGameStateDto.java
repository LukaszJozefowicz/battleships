package com.ljozefowicz.battleships.dto;

import com.ljozefowicz.battleships.model.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentGameStateDto {
    String currentPlayer;
    String opponentPlayer;
    Board currentPlayerBoard;
    Board opponentPlayerBoard;
}
