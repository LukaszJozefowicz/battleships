package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.ShipPlacementInfoDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.dto.ShipPlacementInfoForOpponentDto;
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
        boardService.saveShipField(currentBoard, placementInfo.getType(), placementInfo.getLength(), placementInfo.getCoords());

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

//        Game currentGame = gameService.findGameByUsername(principal.getName());

//        messagingTemplate.convertAndSendToUser(currentGame.getPlayer1().getUsername(), "/queue/sendInfoMessage", msg);
//        messagingTemplate.convertAndSendToUser(currentGame.getPlayer2().getUsername(), "/queue/sendInfoMessage", msg);
    }

    @MessageMapping("/sendCurrentTurn/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public MessageDto sendCurrentTurnInfo(MessageDto messageObj, Principal principal){

        Game currentGame = gameService.findGameByUsername(principal.getName());

        return MessageDto.builder()
                .messageType(messageObj.getMessageType())
                .username(currentGame.getPlayerTurn() == GameTurn.PLAYER1
                            ? currentGame.getPlayer1().getUsername()
                            : currentGame.getPlayer2().getUsername())
                .message(messageObj.getMessage())
                .build();


//        messagingTemplate.convertAndSendToUser(currentGame.getPlayer1().getUsername(), "/queue/sendInfoMessage", msg);
//        messagingTemplate.convertAndSendToUser(currentGame.getPlayer2().getUsername(), "/queue/sendInfoMessage", msg);
    }

}
