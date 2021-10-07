package com.ljozefowicz.battleships.model.beans;

import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PcShipsToAddToDB {

    private List<ShipPlacementInfo> shipsToAddToDB;

    public PcShipsToAddToDB() {
        shipsToAddToDB = new ArrayList<>();
    }
}
