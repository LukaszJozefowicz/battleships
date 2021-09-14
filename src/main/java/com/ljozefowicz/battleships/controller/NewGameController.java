package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.FieldDto;
import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.dto.ShipPlacementInfoDto;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.model.Field;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.util.Counter;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
        return "new-game";
    }
/*
    @GetMapping("/placeShips")
    public ResponseEntity<Void> placeShips(@RequestParam String coords, Model model, Principal principal){

        Game game = gameService.findGameByPlayer1Username(principal.getName());
        if(game == null)
            game = gameService.findGameByPlayer2Username(principal.getName());

        Board board = game.getFirstPlayerBoard();
        if(principal.getName().equals(game.getPlayer2().getUsername()))
            board = game.getSecondPlayerBoard();


        boardService.updateField(board, coords, FieldStatus.SHIP_ALIVE);
        //model.addAttribute("board", boardService.getFieldsList(board));
        //model.addAttribute("counter", new Counter(10));
        //model.addAttribute("shipsToPlace", allowedShipService.getListOfShipsToPlace());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/finishGame")
    public ResponseEntity<Void> finishGame(Principal principal){
        Long gameId = gameService.findGameIdByPlayer1Username(principal.getName());
        gameService.deleteGame(gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }*/

    //--------- webSocket controllers ------------
    @MessageMapping("/sendShipsPlacementInfo/{gameId}")
    //@SendToUser("/newGame/{gameId}")
    public void sendShipsPlacementInfoToOpponent(ShipPlacementInfoDto placementInfo, @DestinationVariable Long gameId, Principal principal){

        Game currentGame = gameService.findGameById(gameId);
        String opponentUser = principal.getName().equals(currentGame.getPlayer1().getUsername()) ?
            currentGame.getPlayer2().getUsername() : currentGame.getPlayer1().getUsername();

        messagingTemplate.convertAndSendToUser(opponentUser, "/queue/sendPlacementInfo/" + gameId, placementInfo);
    }

    @MessageMapping("/shipPlacement")
    public void placeShip(FieldDto field, Principal principal){

        Game currentGame = gameService.findGameByUsername(principal.getName());

        Board currentBoard = principal.getName().equals(currentGame.getPlayer1().getUsername()) ?
                currentGame.getFirstPlayerBoard() : currentGame.getSecondPlayerBoard();

        boardService.updateField(currentBoard, field.getCoords(), FieldStatus.valueOf(field.getFieldStatus())); //FieldStatus.SHIP_ALIVE

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/placeShip", field);
    }

    @MessageMapping("/resetBoard")
    public void resetBoard(Principal principal, MessageDto messageDto){
        Game currentGame = gameService.findGameByUsername(principal.getName());
        Board currentBoard = principal.getName().equals(currentGame.getPlayer1().getUsername()) ?
                currentGame.getFirstPlayerBoard() : currentGame.getSecondPlayerBoard();

        boardService.resetBoard(currentBoard);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/resetBoard", messageDto);
    }

}
