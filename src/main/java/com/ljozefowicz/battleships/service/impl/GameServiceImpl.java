package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.repository.GameRepository;
import com.ljozefowicz.battleships.repository.UserRepository;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final BoardService boardService;

    @Override
    @Transactional
    public Game createNewGame(String username) {

        User currentUser = userService.findByUsername(username);
        Game newGame = Game.builder()
                .player1(currentUser)
                .player2(null)
                .firstPlayerBoard(boardService.initializeBoard())
                .secondPlayerBoard(null)
                .gameState(GameState.WAITING_FOR_PLAYER)
                .playerTurn(new Random().nextBoolean() ? GameTurn.PLAYER1 : GameTurn.PLAYER2)
                .build();

        return gameRepository.save(newGame);
    }

    @Override
    @Transactional
    public Game updateGameData(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    @Override
    public Optional<Game> findGameByPlayer1UsernameNotInGame(String username) {
        User user = userRepository.findByUsername(username).orElseGet(() -> User.builder().id(0L).build());
        return gameRepository.findByPlayer1_idAndGameStateNot(user.getId(), GameState.GAME_IN_PROGRESS);
    }

    @Override
    public Optional<Game> findGameByPlayer2UsernameNotInGame(String username) {
        User user = userRepository.findByUsername(username).orElseGet(() -> User.builder().id(0L).build());
        return gameRepository.findByPlayer2_idAndGameStateNot(user.getId(), GameState.GAME_IN_PROGRESS);
    }

    @Override
    public Optional<Game> findGameByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseGet(() -> User.builder().id(0L).build());
        return gameRepository.findByPlayer1_idOrPlayer2_id(user.getId(), user.getId());
    }

    @Override
    public List<Game> getAvailableGames(){
        return gameRepository.findAll();
    }

    @Override
    public Optional<Game> findGameById(Long id){
        return gameRepository.findById(id);
    }

    @Override
    @Transactional
    public Game joinGameById(Long id, String username){
        Game gameToJoin = gameRepository.findById(id).orElseGet(() -> Game.builder().id(0L).build());
        User currentUser = userRepository.findByUsername(username).orElseGet(() -> User.builder().id(0L).build());
        gameToJoin.setPlayer2(currentUser);
        gameToJoin.setSecondPlayerBoard(boardService.initializeBoard());
        gameToJoin.setGameState(GameState.READY_TO_START);
        return gameRepository.save(gameToJoin);
    }

    @Override
    public Board getBoardByPlayerName(Game game, String username){
        return game.getPlayer1().getUsername().equals(username)
                ? game.getFirstPlayerBoard()
                : game.getSecondPlayerBoard();
    }

    @Override
    public String getActivePlayerUsername(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER1 ? game.getPlayer1().getUsername() : game.getPlayer2().getUsername();
    }

    @Override
    public String getInactivePlayerUsername(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER2 ? game.getPlayer1().getUsername() : game.getPlayer2().getUsername();
    }

    @Override
    public Board getActivePlayerBoard(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER1 ? game.getFirstPlayerBoard() : game.getSecondPlayerBoard();
    }

    @Override
    public Board getInactivePlayerBoard(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER2 ? game.getFirstPlayerBoard() : game.getSecondPlayerBoard();
    }

    @Override
    @Transactional
    public Game switchTurns(Game game){

        game.setPlayerTurn(game.getPlayerTurn() == GameTurn.PLAYER1 ? GameTurn.PLAYER2 : GameTurn.PLAYER1);

        return gameRepository.save(game);
    }
}
