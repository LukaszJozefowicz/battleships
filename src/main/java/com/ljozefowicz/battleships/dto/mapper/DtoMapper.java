package com.ljozefowicz.battleships.dto.mapper;

import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.model.entity.Ship;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DtoMapper {

    public ShipToPlaceDto mapToShipToPlaceDto(AllowedShip ship, int whichOfAKind){
        return ShipToPlaceDto.builder()
                .type(ship.getType())
                .length(ship.getLength())
                .whichOfAKind(whichOfAKind)
                .build();
    }

}
