package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.repository.GameRepository;
import com.ljozefowicz.battleships.repository.UserRepository;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.service.LoggedInService;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GameServiceImpl implements GameService {

    private GameRepository gameRepository;
    private UserRepository userRepository;
    private UserService userService;
    private BoardService boardService;
    private LoggedInService loggedInService;

    @Override
    public Game createNewGame(String username) {

        User currentUser = userService.findByUsername(username);
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
    public Game updateGameState(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    @Override
    public Long findGameIdByPlayer1Username(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        Game game = gameRepository.findByPlayer1_id(user.getId()).orElse(null);
        if(game != null) {
            return game.getId();
        }
        return null;
    }

    @Override
    public Game findGameByPlayer1Username(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return gameRepository.findByPlayer1_id(user.getId()).orElse(null);
    }

    @Override
    public Game findGameByPlayer2Username(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return gameRepository.findByPlayer2_id(user.getId()).orElse(null);
    }

    @Override
    public Game findGameByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return gameRepository.findByPlayer1_idOrPlayer2_id(user.getId(), user.getId()).orElse(null);
    }

    @Override
    public List<Game> getAvailableGames(){
        return gameRepository.findAll();
    }

    @Override
    public Game findGameById(Long id){
        return gameRepository.findById(id).orElse(null);
    }

    @Override
    public Game joinGameById(Long id, String username){
        Game gameToJoin = gameRepository.findById(id).orElse(null);
        User currentUser = userRepository.findByUsername(username).orElse(null);
        gameToJoin.setPlayer2(currentUser);
        gameToJoin.setSecondPlayerBoard(boardService.initializeBoard());
        gameToJoin.setGameState(GameState.READY_TO_START);
        return gameRepository.save(gameToJoin);
    }

    private String getLoggedInUsername(){
//        org.springframework.security.core.userdetails.User user =
//                (org.springframework.security.core.userdetails.User)SecurityContextHolder
//                        .getContext()
//                        .getAuthentication()
//                        .getPrincipal();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return currentPrincipalName;//user.getUsername();
    }
}
