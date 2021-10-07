package com.ljozefowicz.battleships.enums;

public enum FieldStatus {
    EMPTY,
    SHIP_ALIVE,
    SHIP_HIT,
    SHIP_SUNK,
    MISS,
    AROUND_SHIP,
    AROUND_SUNK;

    public static FieldStatus setEmptyIfAroundShip(FieldStatus fieldStatus){
        return fieldStatus == FieldStatus.AROUND_SHIP ? FieldStatus.EMPTY : fieldStatus;
    }
}
