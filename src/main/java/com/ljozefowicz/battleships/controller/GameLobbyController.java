package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
import com.ljozefowicz.battleships.stompMessageObj.Message;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.model.beans.ActiveUsersList;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class GameLobbyController {

    GameService gameService;
    BoardService boardService;
    ActiveUsersList activeUsersList;
    ActiveGamesList activeGamesList;
    DtoMapper dtoMapper;
    SimpMessagingTemplate messagingTemplate;

    public GameLobbyController(GameService gameService, BoardService boardService, ActiveUsersList activeUsersList, ActiveGamesList activeGamesList, DtoMapper dtoMapper, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.boardService = boardService;
        this.activeUsersList = activeUsersList;
        this.activeGamesList = activeGamesList;
        this.dtoMapper = dtoMapper;
        List<GameDto> games = this.gameService.getAvailableGames()
                .stream()
                .map(dtoMapper::mapToGameDto)//g -> dtoMapper.mapToGameDto(g)
                .collect(Collectors.toList());
        this.activeGamesList.getGamesList().addAll(games);
        this.messagingTemplate = messagingTemplate;
    }


    @GetMapping("/gameLobby")
    public String getGameLobby(Principal principal){
        activeUsersList.getUsersList().add(principal.getName());
        return "game-lobby";
    }

    @GetMapping("/")
    public String getMainPage(){
        return "main-menu";
    }

    //------------ webSocket controllers --------------

    @MessageMapping("/listOfUsers")
    @SendTo("/gameLobby")
    public String getListOfActiveUsers(){
        Set<String> loggedUsers = activeUsersList.getUsersList();
        return new Gson().toJson(loggedUsers, Set.class);
    }

    @MessageMapping("/userLeft")
    @SendTo("/gameLobby")
    public String sendUsersListAfterUserLeft(String jsonList){
        Type listType = new TypeToken<HashSet<String>>(){}.getType();
        Set<String> listFromJson = new Gson().fromJson(jsonList, listType);

        activeUsersList.setUsersList(listFromJson);
        return jsonList;
    }

    @MessageMapping("/sendToChat")
    @SendTo("/gameLobby")
    public Message sendChatMessage(Message messageObj){
        return Message.builder()
                .messageType(messageObj.getMessageType())
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/gamesList")
    @SendTo("/gameLobby")
    public String sendGamesList(){

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/newGame")
    @SendTo("/gameLobby")
    public String createNewGame(Principal principal){
        GameDto newGame = gameService.createNewGame(principal.getName());
        activeGamesList.getGamesList().add(newGame);

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/newGameVsPC")
    @SendTo("/gameLobby")
    public String createNewGameVsPC(Principal principal){
        GameDto newGame = gameService.createNewGameVsPC(principal.getName());
        activeGamesList.getGamesList().add(newGame);

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/joinGame/{gameId}")
    @SendTo("/gameLobby")
    public String joinGame(Principal principal, @DestinationVariable Long gameId){
        //add user to created game in db
        gameService.joinGameById(gameId, principal.getName());

        //add user to activeGamesList sent through webSocket
        setPlayerJoinedInfoInActiveGamesList(gameId, principal.getName(), GameState.READY_TO_START);

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/deleteGameOrUserJoined")
    @SendTo("/gameLobby")
    public String deleteGameOrUserJoinedAfterUserLeft(Principal principal){

        //delete game created by user once he left the lobby
        gameService.findGameByPlayer1UsernameNotInGame(principal.getName()).ifPresent(createdGame -> {
            gameService.deleteGame(createdGame);
            activeGamesList.getGamesList().remove(dtoMapper.mapToGameDto(createdGame));
        });

        //remove user from game if he was present in active game as player2 (joined), once he left the lobby
        gameService.findGameByPlayer2UsernameNotInGame(principal.getName()).ifPresent(possibleGameJoined -> {
            final Long boardIdToDelete = possibleGameJoined.getSecondPlayerBoard().getId();
            possibleGameJoined.setPlayer2(null);
            possibleGameJoined.setSecondPlayerBoard(null);
            possibleGameJoined.setGameState(GameState.WAITING_FOR_PLAYER);

            gameService.updateGameData(possibleGameJoined);
            boardService.deleteBoard(boardIdToDelete);

            setPlayerJoinedInfoInActiveGamesList(possibleGameJoined.getId(), "waiting for player", GameState.WAITING_FOR_PLAYER);
        });

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }


    @MessageMapping("/newGame/redirect")
    public void notifyUserNewGameStarted(String msg){
        GameDto gameToStart = new Gson().fromJson(msg, GameDto.class);

        gameService.findGameById(gameToStart.getId()).ifPresent(game -> {
            game.setGameState(GameState.GAME_IN_PROGRESS);
            gameService.updateGameData(game);
        });

        int index = activeGamesList.getGamesList().indexOf(gameToStart);
        activeGamesList.getGamesList().get(index).setGameState(GameState.GAME_IN_PROGRESS.name());

        messagingTemplate.convertAndSendToUser(gameToStart.getPlayer2(), "/queue/notify", msg);
        messagingTemplate.convertAndSendToUser(gameToStart.getPlayer1(), "/queue/notify", msg);
    }

    private void setPlayerJoinedInfoInActiveGamesList(Long gameId, String playerName, GameState gameState){
        GameDto gameDto = activeGamesList.getGamesList()
                .stream()
                .filter(game -> game.getId().equals(gameId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Game you're trying to join doesn't exist anymore"));

        int index = activeGamesList.getGamesList().indexOf(gameDto);
        activeGamesList.getGamesList().get(index).setPlayer2(playerName);
        activeGamesList.getGamesList().get(index).setGameState(gameState.name());
    }
}
