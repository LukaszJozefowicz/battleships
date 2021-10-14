package com.ljozefowicz.battleships.dto.mapper;

import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.dto.SettingsDto;
import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.enums.Difficulty;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.enums.ShipShape;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.model.entity.Game;
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

    public SettingsDto mapToSettingsDto(com.ljozefowicz.battleships.model.entity.Settings settings){
        return SettingsDto.builder()
                .difficulty(settings.getDifficulty().name())
                .startingPlayer(settings.getStartingPlayer().name())
                .shipShape(settings.getShipShape().name())
                .build();
    }

    public com.ljozefowicz.battleships.model.entity.Settings mapToSettings(SettingsDto settingsDto){
        return com.ljozefowicz.battleships.model.entity.Settings.builder()
                .difficulty(Difficulty.valueOf(settingsDto.getDifficulty()))
                .startingPlayer(GameTurn.valueOf(settingsDto.getStartingPlayer()))
                .shipShape(ShipShape.valueOf(settingsDto.getShipShape()))
                .build();
    }

}
