package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;

import java.util.List;
import java.util.Optional;

public interface GameService {
    GameDto createNewGame(String username);
    GameDto createNewGameVsPC(String username);
    void deleteGame(Long id);
    Optional<Game> findGameByPlayer1UsernameNotInGame(String username);
    Optional<Game> findGameByPlayer2UsernameNotInGame(String username);
    Optional<Game> findGameByUsername(String username);
    Optional<Game> findGameById(Long id);
    List<Game> getAvailableGames();
    Game joinGameById(Long id, String username);
    Game updateGameData(Game game);
    Board getBoardByPlayerName(Game game, String username);
    CurrentGameStateDto getCurrentGameState(Long gameId);
    Game switchTurns(Long gameId);
}
