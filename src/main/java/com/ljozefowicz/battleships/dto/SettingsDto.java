package com.ljozefowicz.battleships.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettingsDto {
    private String difficulty;
    private String startingPlayer;
    private String shipShape;
}
