package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.model.entity.Game;

import java.util.List;

public interface GameService {
    Game createNewGame();

    void deleteGame(Long id);
    Long findGameIdByLoggedInUsername();
    Game findGameByLoggedInUsername();
    List<Game> getAvailableGames();
    Game joinGameById(Long id);
}
