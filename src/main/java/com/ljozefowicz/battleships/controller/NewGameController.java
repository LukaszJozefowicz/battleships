package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.*;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.service.UserService;
import com.ljozefowicz.battleships.util.Counter;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@Controller
@AllArgsConstructor
public class NewGameController {

    GameService gameService;
    BoardService boardService;
    UserService userService;
    AllowedShipService allowedShipService;
    SimpMessagingTemplate messagingTemplate;
    ActiveGamesList activeGamesList;
    DtoMapper dtoMapper;

    @GetMapping("/newGame/{gameId}")
    public String createNewGame(Model model, @PathVariable Long gameId, Principal principal){
        gameService.findGameById(gameId).ifPresent(game -> {
            Board playerBoard = principal.getName().equals(game.getPlayer1().getUsername())
                                ? game.getFirstPlayerBoard()
                                : game.getSecondPlayerBoard();
            Board opponentBoard = principal.getName().equals(game.getPlayer1().getUsername())
                                ? game.getSecondPlayerBoard()
                                : game.getFirstPlayerBoard();

            model.addAttribute("board", boardService.getBoardAsArray(playerBoard));
            model.addAttribute("opponentBoard", boardService.getBoardAsArray(opponentBoard));
            model.addAttribute("counter", new Counter(0));
            model.addAttribute("counterOpponent", new Counter(0));
            model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
            model.addAttribute("startingPlayer", game.getPlayerTurn() == GameTurn.PLAYER1
                                                    ? game.getPlayer1().getUsername()
                                                    : game.getPlayer2().getUsername());
            model.addAttribute("opponentName", principal.getName().equals(game.getPlayer1().getUsername())
                                                    ? game.getPlayer2().getUsername()
                                                    : game.getPlayer1().getUsername());
        });
        return "new-game";
    }

    @GetMapping("/newGameVsPC")
    public String createNewGameVsPC(Model model, Principal principal){
//        Game newGame = gameService.createNewGame(principal.getName());
//        User computerPlayer = userService.saveUser(UserRegistrationDto.builder()
//                .username("ComputerEasy")
//                .password("qwerty")
//                .confirmPassword("qwerty")
//                .email("bot@bot.pl")
//                .confirmEmail("bot@bot.pl")
//                .build());

//        User computerPlayer = userService.findByUsername("ComputerEasy");
//        newGame = gameService.joinGameById(newGame.getId(), computerPlayer.getUsername());
        Game newGame = gameService.createNewGameVsPC(principal.getName());

        model.addAttribute("gameId", newGame.getId());
        model.addAttribute("board", boardService.getBoardAsArray(newGame.getFirstPlayerBoard()));
        model.addAttribute("opponentBoard", boardService.getBoardAsArray(newGame.getSecondPlayerBoard()));
        model.addAttribute("counter", new Counter(0));
        model.addAttribute("counterOpponent", new Counter(0));
        model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
        model.addAttribute("shipsToPlacePC", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
        model.addAttribute("startingPlayer", newGame.getPlayerTurn() == GameTurn.PLAYER1
                ? newGame.getPlayer1().getUsername()
                : newGame.getPlayer2().getUsername());
        model.addAttribute("opponentName", newGame.getPlayer2().getUsername());
        return "new-game";
    }

    //--------- webSocket controllers ------------
    @MessageMapping("/sendShipsPlacementInfo/{gameId}")
    public void sendShipsPlacementInfoToOpponent(ShipPlacementInfoForOpponentDto placementInfo, @DestinationVariable Long gameId, Principal principal){

        gameService.findGameById(gameId).ifPresent(currentGame -> {
            String opponentUser = principal.getName().equals(currentGame.getPlayer1().getUsername())
                                ? currentGame.getPlayer2().getUsername()
                                : currentGame.getPlayer1().getUsername();

//            if("ComputerEasy".equals(opponentUser))
//                opponentUser = principal.getName();
            messagingTemplate.convertAndSendToUser(opponentUser, "/queue/sendPlacementInfo/" + gameId, placementInfo);
        });
    }

    @MessageMapping("/shipPlacement")
    public void placeShip(ShipPlacementInfoDto placementInfo, Principal principal){

        Game currentGame = gameService.findGameByUsername(principal.getName())
                            .orElseThrow(() -> new EntityNotFoundException("Game you entered doesn't exist anymore"));

        Board currentBoard = gameService.getBoardByPlayerName(currentGame, principal.getName());

        boardService.updateField(currentBoard, placementInfo.getCoords(), FieldStatus.SHIP_ALIVE); //FieldStatus.SHIP_ALIVE || FieldStatus.valueOf(placementInfo.getFieldStatus())
        boardService.addShipField(currentBoard, placementInfo.getType(), placementInfo.getLength(), placementInfo.getCoords());

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/placeShip", placementInfo);
    }

    @MessageMapping("/resetBoard")
    public void resetBoard(Principal principal, MessageDto messageDto){
        Game currentGame = gameService.findGameByUsername(principal.getName())
                            .orElseThrow(() -> new EntityNotFoundException("Game you entered doesn't exist anymore"));
        Board currentBoard = principal.getName().equals(currentGame.getPlayer1().getUsername())
                            ? currentGame.getFirstPlayerBoard()
                            : currentGame.getSecondPlayerBoard();

        boardService.resetBoard(currentBoard);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/resetBoard", messageDto);
    }

    @MessageMapping("/gameChat/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public MessageDto sendChatMessage(MessageDto messageObj){
        return MessageDto.builder()
                .messageType(messageObj.getMessageType())
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/leaveGame/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public MessageDto leaveGame(MessageDto messageObj, @DestinationVariable Long gameId){

        gameService.findGameById(gameId).ifPresent(currentGame -> {
            gameService.deleteGame(currentGame.getId());
            activeGamesList.getGamesList().remove(dtoMapper.mapToGameDto(currentGame));
        });

        return MessageDto.builder()
                .messageType(messageObj.getMessageType())
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/shoot/{gameId}")
    @SendTo("/newGame/{gameId}/shoot")
    public ShotInfoDto shoot(ShotInfoDto shotInfo, @DestinationVariable Long gameId) throws InterruptedException{

        Game currentGame = gameService.findGameById(gameId)
                        .orElseThrow(() -> new EntityNotFoundException("Game with id: " + gameId + " not found in db"));

        String currentPlayer = gameService.getActivePlayerUsername(currentGame);
        String opponentPlayer = gameService.getInactivePlayerUsername(currentGame);
        Board currentPlayerBoard = gameService.getActivePlayerBoard(currentGame);
        Board opponentPlayerBoard = gameService.getInactivePlayerBoard(currentGame);

        //-------------

        if(currentPlayer.equals("ComputerEasy") && currentGame.getPlayerTurn() == GameTurn.PLAYER2){
            Thread.sleep(2000);
            System.out.println("computer iz shootin");
            shotInfo.setCoords(boardService.getRandomEmptyTile(boardService.getBoardAsArray(currentPlayerBoard)));
        }

        //-------------

        String shotResult = boardService.getShotResult(opponentPlayerBoard, shotInfo.getCoords());
        String sunkShipCoords = "";
        String shipFieldsToReveal = "";
        boolean isAllShipsSunk = false;

        boardService.updateField(opponentPlayerBoard, shotInfo.getCoords(), FieldStatus.valueOf(shotResult));

        if(shotResult.equals(FieldStatus.SHIP_SUNK.name())){
            sunkShipCoords = boardService.getFieldsOfShipByCoords(opponentPlayerBoard, shotInfo.getCoords());
            isAllShipsSunk = boardService.checkIfAllShipsAreSunk(opponentPlayerBoard.getId());
            if(isAllShipsSunk) {
                shipFieldsToReveal = boardService.getShipFieldsToReveal(currentPlayerBoard.getId());
            }
        }
        if(!isAllShipsSunk)
            gameService.switchTurns(currentGame);

        return ShotInfoDto.builder()
                .currentPlayer(currentPlayer)
                .opponentPlayer(opponentPlayer)
                .shotResult(shotResult)
                .coords(shotInfo.getCoords())
                .sunkShipCoords(sunkShipCoords)
                .isAllShipsSunk(isAllShipsSunk)
                .shipFieldsToReveal(shipFieldsToReveal)
                .build();
    }

//    @MessageMapping("/shoot/{gameId}")
//    @SendTo("/newGameVsPC/{gameId}/shoot")
//    public ShotInfoDto shootPhaseVsPC(ShotInfoDto shotInfo, @DestinationVariable Long gameId){
//
//        Game currentGame = gameService.findGameById(gameId)
//                .orElseThrow(() -> new EntityNotFoundException("Game with id: " + gameId + " not found in db"));
//
//        String currentPlayer = gameService.getActivePlayerUsername(currentGame);
//        String opponentPlayer = gameService.getInactivePlayerUsername(currentGame);
//        Board currentPlayerBoard = gameService.getActivePlayerBoard(currentGame);
//        Board opponentPlayerBoard = gameService.getInactivePlayerBoard(currentGame);
//
//        String shotResult = boardService.getShotResult(opponentPlayerBoard, shotInfo.getCoords());
//        String sunkShipCoords = "";
//        String shipFieldsToReveal = "";
//        boolean isAllShipsSunk = false;
//
//        boardService.updateField(opponentPlayerBoard, shotInfo.getCoords(), FieldStatus.valueOf(shotResult));
//
//        if(shotResult.equals(FieldStatus.SHIP_SUNK.name())){
//            sunkShipCoords = boardService.getFieldsOfShipByCoords(opponentPlayerBoard, shotInfo.getCoords());
//            isAllShipsSunk = boardService.checkIfAllShipsAreSunk(opponentPlayerBoard.getId());
//            if(isAllShipsSunk) {
//                shipFieldsToReveal = boardService.getShipFieldsToReveal(currentPlayerBoard.getId());
//            }
//        }
//        if(!isAllShipsSunk)
//            gameService.switchTurns(currentGame);
//
//
//        return ShotInfoDto.builder()
//                .currentPlayer(currentPlayer)
//                .opponentPlayer(opponentPlayer)
//                .shotResult(shotResult)
//                .coords(shotInfo.getCoords())
//                .sunkShipCoords(sunkShipCoords)
//                .isAllShipsSunk(isAllShipsSunk)
//                .shipFieldsToReveal(shipFieldsToReveal)
//                .build();
//    }
}
