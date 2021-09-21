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
        gamesList.add(GameDto.builder()     //a little trick so I can check in javascript what kind of objects are passed
                .id(0L)
                .player1("string")
                .player2("string")
                .gameState("string")
                .build());
    }
}
