package com.ljozefowicz.battleships.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipToPlaceDto {

    private String type;
    private int length;
    private int whichOfAKind;
}
