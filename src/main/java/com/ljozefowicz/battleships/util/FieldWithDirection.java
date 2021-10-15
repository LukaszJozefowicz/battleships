package com.ljozefowicz.battleships.util;

import com.ljozefowicz.battleships.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FieldWithDirection {
    private String coords;
    private Direction direction;
}
