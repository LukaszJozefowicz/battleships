package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;

import java.util.List;
import java.util.Optional;

public interface GameService {
    Game createNewGame(String username);

    void deleteGame(Long id);
    Long findGameIdByPlayer1Username(String username);
    Optional<Game> findGameByPlayer1Username(String username);
    Game findGameByPlayer2Username(String username);
    Optional<Game> findGameByPlayer1UsernameNotInGame(String username);
    Optional<Game> findGameByPlayer2UsernameNotInGame(String username);
    Game findGameByUsername(String username);
    List<Game> getAvailableGames();
    Game joinGameById(Long id, String username);
    Game updateGameState(Game game);
    Optional<Game> findGameById(Long id);
    Board getBoardByPlayerName(Game game, String username);
    Board getActivePlayerBoard(Game game);
    Board getInactivePlayerBoard(Game game);
    String getActivePlayerUsername(Game game);
    String getInactivePlayerUsername(Game game);
    Game switchTurns(Game game);
}
