package com.ljozefowicz.battleships.dto.mapper;

import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.Ship;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DtoMapper {

    public ShipToPlaceDto mapToShipToPlaceDto(AllowedShip ship, int whichOfAKind){
        return ShipToPlaceDto.builder()
                .type(ship.getType())
                .length(ship.getLength())
                .whichOfAKind(whichOfAKind)
                .build();
    }

    public GameDto mapToGameDto(Game game){

        final String SECOND_PLAYER_NONE = "waiting for player";

        return GameDto.builder()
                .id(game.getId())
                .player1(game.getPlayer1().getUsername())
                .player2(game.getPlayer2() != null ? game.getPlayer2().getUsername() : SECOND_PLAYER_NONE)
                .gameState(game.getGameState().name())
                .build();
    }

}
