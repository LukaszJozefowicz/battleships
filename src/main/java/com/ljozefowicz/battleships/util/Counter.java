package com.ljozefowicz.battleships.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Counter {

    private int value;

    public int increment(){
        value = ++value;
        return value;
    }

}
