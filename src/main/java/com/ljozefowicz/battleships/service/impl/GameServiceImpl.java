package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
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
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public GameDto createNewGame(String username) {

        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User: " + username + " not found in db"));
        Game newGame = Game.builder()
                .player1(currentUser)
                .player2(null)
                .firstPlayerBoard(boardService.initializeBoard())
                .secondPlayerBoard(null)
                .gameState(GameState.WAITING_FOR_PLAYER)
                .playerTurn(new Random().nextBoolean() ? GameTurn.PLAYER1 : GameTurn.PLAYER2)
                .build();

        gameRepository.save(newGame);
        return dtoMapper.mapToGameDto(newGame);
    }

    @Override
    @Transactional
    public GameDto createNewGameVsPC(String username) {

        boardService.clearPcShipsToAddList();

        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User: " + username + " not found in db"));
        User computerPlayer = userService.saveUser(new UserRegistrationDto(), UserRole.ROLE_BOT_EASY);
        Game newGame = Game.builder()
                .player1(currentUser)
                .player2(computerPlayer)
                .firstPlayerBoard(boardService.initializeBoard())
                .secondPlayerBoard(boardService.initializeComputerBoard())
                .gameState(GameState.READY_TO_START)
                .playerTurn(new Random().nextBoolean() ? GameTurn.PLAYER1 : GameTurn.PLAYER2)
                .build();

        boardService.savePcShipsToDB(newGame.getSecondPlayerBoard());

        gameRepository.save(newGame);
        return dtoMapper.mapToGameDto(newGame);
    }

    @Override
    @Transactional
    public Game updateGameData(Game game) {
        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteGame(Long id) {

        Game game = gameRepository.getById(id);

        gameRepository.deleteById(id);

        String username = game.getPlayer2().getUsername();
        userService.deleteBotUserIfPresent(username);
    }

    @Override
    public Optional<Game> findGameByPlayer1UsernameNotInGame(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name: " + username + " not found in db"));
        return gameRepository.findByPlayer1_idAndGameStateNot(user.getId(), GameState.GAME_IN_PROGRESS);
    }

    @Override
    public Optional<Game> findGameByPlayer2UsernameNotInGame(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name: " + username + " not found in db"));
        return gameRepository.findByPlayer2_idAndGameStateNot(user.getId(), GameState.GAME_IN_PROGRESS);
    }

    @Override
    public Optional<Game> findGameByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name: " + username + " not found in db"));
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
        Game gameToJoin = gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game with id: " + id + " not found in db"));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name: " + username + " not found in db"));
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
    @Transactional
    public Game switchTurns(Long gameId){

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game with id: " + gameId + " not found in db"));

        game.setPlayerTurn(game.getPlayerTurn() == GameTurn.PLAYER1 ? GameTurn.PLAYER2 : GameTurn.PLAYER1);

        return gameRepository.save(game);
    }

    @Override
    public CurrentGameStateDto getCurrentGameState(Long gameId){

        Game currentGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game with id: " + gameId + " not found in db"));

        return CurrentGameStateDto.builder()
                .currentPlayer(getActivePlayerUsername(currentGame))
                .opponentPlayer(getInactivePlayerUsername(currentGame))
                .currentPlayerBoard(getActivePlayerBoard(currentGame))
                .opponentPlayerBoard(getInactivePlayerBoard(currentGame))
                .build();
    }

    private String getActivePlayerUsername(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER1 ? game.getPlayer1().getUsername() : game.getPlayer2().getUsername();
    }

    private String getInactivePlayerUsername(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER2 ? game.getPlayer1().getUsername() : game.getPlayer2().getUsername();
    }

    private Board getActivePlayerBoard(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER1 ? game.getFirstPlayerBoard() : game.getSecondPlayerBoard();
    }

    private Board getInactivePlayerBoard(Game game){
        return game.getPlayerTurn() == GameTurn.PLAYER2 ? game.getFirstPlayerBoard() : game.getSecondPlayerBoard();
    }
}
