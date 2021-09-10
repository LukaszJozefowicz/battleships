package com.ljozefowicz.battleships.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShipPlacementInfoDto {
    private String shipName;
    private String whichOfAKind;
    private String isAllShipsPlaced;
}
