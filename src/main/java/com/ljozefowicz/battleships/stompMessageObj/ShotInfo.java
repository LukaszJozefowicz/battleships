package com.ljozefowicz.battleships.stompMessageObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShotInfo {
    private String currentPlayer;
    private String opponentPlayer;
    private String shotResult;
    private String sunkShipCoords;
    private boolean isAllShipsSunk;
    private String shipFieldsToReveal;
    private String coords;
}
