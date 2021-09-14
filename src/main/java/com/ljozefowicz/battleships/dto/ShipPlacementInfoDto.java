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
    private String fieldStatus;
    private String type;
    private int length;
    private String coords;
}
