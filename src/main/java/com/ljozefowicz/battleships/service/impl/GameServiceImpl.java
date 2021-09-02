package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.repository.GameRepository;
import com.ljozefowicz.battleships.repository.UserRepository;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GameServiceImpl implements GameService {

    private GameRepository gameRepository;
    private UserRepository userRepository;
    private UserService userService;
    private BoardService boardService;

    @Override
    public Game createNewGame() {

        String loggedInUsername = getLoggedInUsername();
        User currentUser = userService.findByUsername(loggedInUsername);
        Game newGame = Game.builder()
                .player1(currentUser)
                .player2(null)
                .firstPlayerBoard(boardService.initializeBoard())
                .secondPlayerBoard(null)
                .gameState(GameState.WAITING_FOR_PLAYERS)
                .playerTurn(GameTurn.PLAYER1)
                .build();

        return gameRepository.save(newGame);
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    @Override
    public Long findGameIdByLoggedInUsername() {
        String loggedPlayerUsername = getLoggedInUsername();
        User user = userRepository.findByUsername(loggedPlayerUsername);
        Game game = gameRepository.findByPlayer1_id(user.getId());
        return game.getId();
    }

    @Override
    public Game findGameByLoggedInUsername() {
        String loggedPlayerUsername = getLoggedInUsername();
        User user = userRepository.findByUsername(loggedPlayerUsername);
        return gameRepository.findByPlayer1_id(user.getId());
    }

    @Override
    public List<Game> getAvailableGames(){
        return gameRepository.findAll();
    }

    @Override
    public Game joinGameById(Long id){
        Game gameToJoin = gameRepository.findById(id).orElse(null);
        User currentUser = userRepository.findByUsername(getLoggedInUsername());
        gameToJoin.setPlayer2(currentUser);
        gameToJoin.setSecondPlayerBoard(boardService.initializeBoard());
        return gameRepository.save(gameToJoin);
    }

    private String getLoggedInUsername(){
        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User)SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        return user.getUsername();
    }
}
