package com.ljozefowicz.battleships.model.beans;

import com.ljozefowicz.battleships.dto.GameDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ActiveGamesList {

    private List<GameDto> gamesList;

    public ActiveGamesList(){
        gamesList = new ArrayList<>();
        gamesList.add(GameDto.builder()
                .id(0L)
                .player1("string")
                .player2("string")
                .gameState("string")
                .build());
    }
}
