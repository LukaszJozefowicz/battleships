package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.ShipToPlaceDto;

import java.util.List;

public interface AllowedShipService {
    List<ShipToPlaceDto> getListOfShipsToPlace();
//    int countAllowedShipsTotalTiles(List<ShipToPlaceDto> shipsToPlace);
}
