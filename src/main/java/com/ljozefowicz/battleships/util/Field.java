package com.ljozefowicz.battleships.util;

import com.ljozefowicz.battleships.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Field {
    private String coords;
    private Direction direction;
}
