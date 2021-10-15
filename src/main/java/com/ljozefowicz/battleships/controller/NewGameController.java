package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.exception.RandomSetBoardFailedException;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.service.UserService;
import com.ljozefowicz.battleships.stompMessageObj.Message;
import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfoForOpponent;
import com.ljozefowicz.battleships.stompMessageObj.ShotInfo;
import com.ljozefowicz.battleships.util.Counter;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.ljozefowicz.battleships.enums.UserRole.isBot;


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
    public String getNewGameScreen(Model model, @PathVariable Long gameId, Principal principal){
        gameService.findGameById(gameId).ifPresent(game -> {
            FieldStatus[][] playerBoardAsArray = principal.getName().equals(game.getPlayer1().getUsername())
                                ? boardService.getBoardAsArray(game.getFirstPlayerBoard())
                                : boardService.getBoardAsArray(game.getSecondPlayerBoard());
            FieldStatus[][] opponentBoardAsArray = principal.getName().equals(game.getPlayer1().getUsername())
                                ? boardService.getBoardAsArray(game.getSecondPlayerBoard())
                                : boardService.getBoardAsArray(game.getFirstPlayerBoard());

            model.addAttribute("board", playerBoardAsArray);
            model.addAttribute("opponentBoard", opponentBoardAsArray);
            model.addAttribute("rowsCounter", new Counter(0));
            model.addAttribute("rowsCounterOpponent", new Counter(0));
            model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
            model.addAttribute("startingPlayer", game.getPlayerTurn() == GameTurn.PLAYER1
                                                    ? game.getPlayer1().getUsername()
                                                    : game.getPlayer2().getUsername());
            model.addAttribute("opponentName", principal.getName().equals(game.getPlayer1().getUsername())
                                                    ? game.getPlayer2().getUsername()
                                                    : game.getPlayer1().getUsername());
            model.addAttribute("settings", new Gson().toJson(dtoMapper.mapToSettingsDto(userService.getUserSettings(game.getPlayer1().getUsername()))));
        });
        return "new-game";
    }

    //--------- webSocket controllers ------------
    @MessageMapping("/sendShipsPlacementInfo/{gameId}")
    public void sendShipsPlacementInfoToOpponent(ShipPlacementInfoForOpponent placementInfo, @DestinationVariable Long gameId, Principal principal){

        gameService.findGameById(gameId).ifPresent(currentGame -> {
            String opponentUser = gameService.getOpponentName(currentGame, principal.getName());
            messagingTemplate.convertAndSendToUser(opponentUser, "/queue/sendPlacementInfo/" + gameId, placementInfo);
        });
    }

    @MessageMapping("/shipPlacement/{gameId}")
    public void placeShip(ShipPlacementInfo placementInfo, Principal principal, @DestinationVariable Long gameId){

        Board currentBoard = gameService.getBoardByUsername(gameId, principal.getName());

        boardService.updateField(currentBoard, placementInfo.getCoords(), FieldStatus.SHIP_ALIVE);
        boardService.addShipField(currentBoard, placementInfo);

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/placeShip/" + gameId, placementInfo);
    }

    @MessageMapping("/resetBoard/{gameId}")
    public void resetBoard(Principal principal, Message message, @DestinationVariable Long gameId){

        Board currentBoard = gameService.getBoardByUsername(gameId, principal.getName());
        boardService.resetBoard(currentBoard);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/resetBoard/" + gameId, message);
    }

    @MessageMapping("/autoPlacement/{gameId}")
    public void autoFillBoard(Principal principal, @DestinationVariable Long gameId){

        Board currentBoard = gameService.getBoardByUsername(gameId, principal.getName());
        boardService.resetBoard(currentBoard);
        currentBoard = boardService.autoInitializeBoard(gameService.getGameSettings(gameId).getShipShape(), currentBoard);

        FieldStatus[][] fieldStatusArray = boardService.getBoardAsArray(currentBoard);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/autoPlacement/" + gameId, new Gson().toJson(fieldStatusArray));
    }

    @MessageMapping("/gameChat/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public Message sendChatMessage(Message messageObj){
        return Message.builder()
                .messageType(messageObj.getMessageType())
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/leaveGame/{gameId}")
    @SendTo("/sendInfoMessage/{gameId}")
    public Message leaveGame(Message messageObj, @DestinationVariable Long gameId){

        gameService.findGameById(gameId).ifPresent(currentGame -> {
            gameService.deleteGame(currentGame);
            activeGamesList.getGamesList().remove(dtoMapper.mapToGameDto(currentGame));
        });

        return Message.builder()
                .messageType(messageObj.getMessageType())
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/shoot/{gameId}")
    public void shoot(ShotInfo shotInfo, @DestinationVariable Long gameId) {

        CurrentGameStateDto currentGameState = gameService.getCurrentGameState(gameId);

        if(UserRole.isBot(currentGameState.getCurrentPlayer())){

            FieldStatus[][] playerBoard = boardService.getBoardAsArray(currentGameState.getOpponentPlayerBoard());
            switch (currentGameState.getSettings().getDifficulty()){
                case EASY:
                    shotInfo.setCoords(boardService.getRandomTarget(playerBoard));
                    break;
                case NORMAL:
                case HARD:
                    shotInfo.setCoords(boardService.getRandomTargetPossiblyNearShipHit(playerBoard, currentGameState.getSettings().getShipShape()));
                    break;
            }
        }

        String shotResult = boardService.getShotResult(currentGameState.getOpponentPlayerBoard(), shotInfo.getCoords());
        boardService.updateField(currentGameState.getOpponentPlayerBoard(), shotInfo.getCoords(), FieldStatus.valueOf(shotResult));
        shotInfo = boardService.getShotInfo(shotInfo, shotResult, currentGameState);

        if(!shotInfo.isAllShipsSunk())
            gameService.switchTurns(gameId);

        if(!isBot(shotInfo.getCurrentPlayer())) {
            messagingTemplate.convertAndSendToUser(shotInfo.getCurrentPlayer(), "/queue/newGame/" + gameId + "/shoot", shotInfo);
        }
        if(!isBot(shotInfo.getOpponentPlayer()) || !shotInfo.isAllShipsSunk()) {
            messagingTemplate.convertAndSendToUser(shotInfo.getOpponentPlayer(), "/queue/newGame/" + gameId + "/shoot", shotInfo);
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String handleBoardRandomSetupException(RandomSetBoardFailedException ex) {
        return ex.getMessage();
    }
}
