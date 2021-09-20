package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.ShipPlacementInfoDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.dto.ShipPlacementInfoForOpponentDto;
import com.ljozefowicz.battleships.dto.ShotInfoDto;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
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
    AllowedShipService allowedShipService;
    SimpMessagingTemplate messagingTemplate;

    @GetMapping("/newGame/{gameId}")
    public String createNewGame(Model model, @PathVariable Long gameId, Principal principal){
        Game game = gameService.findGameById(gameId);
        Board playerBoard, opponentBoard;
        if(principal.getName().equals(game.getPlayer1().getUsername())){
            playerBoard = game.getFirstPlayerBoard();
            opponentBoard = game.getSecondPlayerBoard();
        } else {
            playerBoard = game.getSecondPlayerBoard();
            opponentBoard = game.getFirstPlayerBoard();
        }
        model.addAttribute("gameId", gameId);
        model.addAttribute("board", boardService.getFieldsList(playerBoard));
        model.addAttribute("opponentBoard", boardService.getFieldsList(opponentBoard));
        model.addAttribute("counter", new Counter(0));
        model.addAttribute("counterOpponent", new Counter(0));
        model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));

        System.out.println("shipsToPlace: " + new Gson().toJson(allowedShipService.getListOfShipsToPlace()));

        model.addAttribute("startingPlayer", game.getPlayerTurn() == GameTurn.PLAYER1
                                                ? game.getPlayer1().getUsername()
                                                : game.getPlayer2().getUsername());
        model.addAttribute("opponentName", principal.getName().equals(game.getPlayer1().getUsername())
                ? game.getPlayer2().getUsername()
                : game.getPlayer1().getUsername());
        return "new-game";
    }

    //--------- webSocket controllers ------------
    @MessageMapping("/sendShipsPlacementInfo/{gameId}")
    //@SendToUser("/newGame/{gameId}")
    public void sendShipsPlacementInfoToOpponent(ShipPlacementInfoForOpponentDto placementInfo, @DestinationVariable Long gameId, Principal principal){

        Game currentGame = gameService.findGameById(gameId);
        String opponentUser = principal.getName().equals(currentGame.getPlayer1().getUsername()) ?
            currentGame.getPlayer2().getUsername() : currentGame.getPlayer1().getUsername();

        messagingTemplate.convertAndSendToUser(opponentUser, "/queue/sendPlacementInfo/" + gameId, placementInfo);
    }

    @MessageMapping("/shipPlacement")
    public void placeShip(ShipPlacementInfoDto placementInfo, Principal principal){

        Game currentGame = gameService.findGameByUsername(principal.getName());

        Board currentBoard = gameService.getBoardByPlayerName(currentGame, principal.getName());


        boardService.updateField(currentBoard, placementInfo.getCoords(), FieldStatus.valueOf(placementInfo.getFieldStatus())); //FieldStatus.SHIP_ALIVE
        boardService.addShipField(currentBoard, placementInfo.getType(), placementInfo.getLength(), placementInfo.getCoords());

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/placeShip", placementInfo);
    }

    @MessageMapping("/resetBoard")
    public void resetBoard(Principal principal, MessageDto messageDto){
        Game currentGame = gameService.findGameByUsername(principal.getName());
        Board currentBoard = principal.getName().equals(currentGame.getPlayer1().getUsername()) ?
                currentGame.getFirstPlayerBoard() : currentGame.getSecondPlayerBoard();

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

    @MessageMapping("/sendCurrentTurn/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public MessageDto sendCurrentTurnInfo(MessageDto messageObj, @DestinationVariable Long gameId){

        Game currentGame = gameService.findGameById(gameId);

        return MessageDto.builder()
                .messageType(messageObj.getMessageType())
                .username(currentGame.getPlayerTurn() == GameTurn.PLAYER1
                            ? currentGame.getPlayer1().getUsername()
                            : currentGame.getPlayer2().getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/shoot/{gameId}")
    @SendTo("/newGame/{gameId}/shoot")
    public ShotInfoDto shoot(ShotInfoDto shotInfo, @DestinationVariable Long gameId){

        Game currentGame = gameService.findGameById(gameId);

        String currentPlayer = gameService.getActivePlayerUsername(currentGame);
        String opponentPlayer = gameService.getInactivePlayerUsername(currentGame);
        Board currentPlayerBoard = gameService.getActivePlayerBoard(currentGame);
        Board opponentPlayerBoard = gameService.getInactivePlayerBoard(currentGame);

        String shotResult = boardService.getShotResult(opponentPlayerBoard, shotInfo.getCoords());
        String sunkShipCoords = "";
        String shipFieldsToReveal = "";
        boolean isAllShipsSunk = false;

        boardService.updateField(opponentPlayerBoard, shotInfo.getCoords(), FieldStatus.valueOf(shotResult));

        if(shotResult == FieldStatus.SHIP_HIT.name() && boardService.checkIfShipIsSunk(opponentPlayerBoard, shotInfo.getCoords())){
            shotResult = FieldStatus.SHIP_SUNK.name();
            sunkShipCoords = boardService.getFieldsOfShipByCoords(opponentPlayerBoard, shotInfo.getCoords());
            isAllShipsSunk = boardService.checkIfAllShipsAreSunk(opponentPlayerBoard.getId());
            if(isAllShipsSunk) {
                shipFieldsToReveal = boardService.getShipFieldsToReveal(currentPlayerBoard.getId());
                gameService.deleteGame(gameId);
            }
        }
        if(!isAllShipsSunk)
            gameService.switchTurns(currentGame);

//        System.out.println("sent shotInfo at end of shoot controller\n" + ShotInfoDto.builder()
//                .currentPlayer(currentPlayer)
//                .opponentPlayer(opponentPlayer)
//                .shotResult(shotResult)
//                .coords(shotInfo.getCoords())
//                .sunkShipCoords(sunkShipCoords)
//                .isAllShipsSunk(isAllShipsSunk)
//                .shipFieldsToReveal(shipFieldsToReveal)
//                .build());

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
}
