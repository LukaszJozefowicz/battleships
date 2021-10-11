package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.enums.GameTurn;
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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    public String createNewGame(Model model, @PathVariable Long gameId, Principal principal){
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

        boardService.updateField(currentBoard, placementInfo.getCoords(), FieldStatus.SHIP_ALIVE); //FieldStatus.SHIP_ALIVE || FieldStatus.valueOf(placementInfo.getFieldStatus())
        boardService.addShipField(currentBoard, placementInfo);

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/placeShip/" + gameId, placementInfo);
    }

    @MessageMapping("/resetBoard/{gameId}")
    public void resetBoard(Principal principal, Message message, @DestinationVariable Long gameId){

        Board currentBoard = gameService.getBoardByUsername(gameId, principal.getName());
        boardService.resetBoard(currentBoard);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/resetBoard/" + gameId, message);
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

        if(isBot(currentGameState.getCurrentPlayer())){
            shotInfo.setCoords(boardService.getRandomTarget(boardService.getBoardAsArray(currentGameState.getOpponentPlayerBoard())));
        }

        String shotResult = boardService.getShotResult(currentGameState.getOpponentPlayerBoard(), shotInfo.getCoords());
        boardService.updateField(currentGameState.getOpponentPlayerBoard(), shotInfo.getCoords(), FieldStatus.valueOf(shotResult));
        shotInfo = boardService.setShotInfo(shotInfo, shotResult, currentGameState);

        if(!shotInfo.isAllShipsSunk())
            gameService.switchTurns(gameId);

        if(!isBot(shotInfo.getCurrentPlayer())) {
            messagingTemplate.convertAndSendToUser(shotInfo.getCurrentPlayer(), "/queue/newGame/" + gameId + "/shoot", shotInfo);
        }
        if(!isBot(shotInfo.getOpponentPlayer()) || !shotInfo.isAllShipsSunk()) {
            messagingTemplate.convertAndSendToUser(shotInfo.getOpponentPlayer(), "/queue/newGame/" + gameId + "/shoot", shotInfo);
        }
    }

    /*@MessageMapping("/shoot/{gameId}")
    @SendTo("/newGame/{gameId}/shoot")
    public ShotInfoDto shoot(ShotInfoDto shotInfo, @DestinationVariable Long gameId) throws InterruptedException{

        Game currentGame = gameService.findGameById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game with id: " + gameId + " not found in db"));

        String currentPlayer = gameService.getActivePlayerUsername(currentGame);
        String opponentPlayer = gameService.getInactivePlayerUsername(currentGame);
        Board currentPlayerBoard = gameService.getActivePlayerBoard(currentGame);
        Board opponentPlayerBoard = gameService.getInactivePlayerBoard(currentGame);

        //-------------

        if(opponentPlayer.equals("ComputerEasy")){
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
    }*/
}
