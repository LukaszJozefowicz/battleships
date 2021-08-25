package com.ljozefowicz.battleships.model;

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
