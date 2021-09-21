package com.ljozefowicz.battleships.util;

import com.ljozefowicz.battleships.enums.FieldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Field {

    private FieldStatus fieldStatus;
    private String coords;
    private int x;
    private int y;
}
