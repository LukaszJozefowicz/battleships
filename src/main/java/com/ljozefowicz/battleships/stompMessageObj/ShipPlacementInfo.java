package com.ljozefowicz.battleships.stompMessageObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShipPlacementInfo {
    private String fieldStatus;
    private String type;
    private int length;
    private String coords;
}
