package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.model.entity.Game;

import java.util.List;

public interface GameService {
    Game createNewGame(String username);

    void deleteGame(Long id);
    Long findGameIdByPlayer1Username(String username);
    Game findGameByPlayer1Username(String username);
    Game findGameByPlayer2Username(String username);
    List<Game> getAvailableGames();
    Game joinGameById(Long id, String username);
    Game updateGameState(Game game);
    Game findGameById(Long id);
}