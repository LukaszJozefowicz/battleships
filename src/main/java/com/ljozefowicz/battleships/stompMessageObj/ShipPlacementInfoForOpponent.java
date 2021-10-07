package com.ljozefowicz.battleships.stompMessageObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShipPlacementInfoForOpponent {
    private String shipName;
    private String whichOfAKind;
    private String isAllShipsPlaced;
}
