package com.ljozefowicz.battleships.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShotInfoDto {
    private String currentPlayer;
    private String opponentPlayer;
    private String shotResult;
    private String coords;
}
