package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ljozefowicz.battleships.dto.GameDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.model.beans.ActiveUsersList;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GameLobbyController {

    GameService gameService;
    ActiveUsersList activeUsersList;
    ActiveGamesList activeGamesList;
    DtoMapper dtoMapper;
    SimpMessagingTemplate messagingTemplate;

    public GameLobbyController(GameService gameService, ActiveUsersList activeUsersList, ActiveGamesList activeGamesList, DtoMapper dtoMapper, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
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

    @MessageMapping("/listOfUsers")
    @SendTo("/gameLobby")
    public String getListOfActiveUsers(){
        List<String> loggedUsers = activeUsersList.getUsersList();
        return new Gson().toJson(loggedUsers, List.class);
    }

    @MessageMapping("/userLeft")
    @SendTo("/gameLobby")
    public String sendUsersListAfterUserLeft(String jsonList){
//        System.out.println("user left json list string: " + jsonList);
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
        List<String> listFromJson = new Gson().fromJson(jsonList, listType);
        System.out.println("java list");
//        for (String s : listFromJson){
//            System.out.println(s + " ");
//        }
        activeUsersList.setUsersList(listFromJson);
        return jsonList;
    }

    @MessageMapping("/sendToChat")
    @SendTo("/gameLobby")
    public MessageDto sendChatMessage(MessageDto messageObj){
        return MessageDto.builder()
                .username(messageObj.getUsername())
                .message(messageObj.getMessage())
                .build();
    }

    @MessageMapping("/gamesList")
    @SendTo("/gameLobby")
    public String sendGamesList(){

        System.out.println("available games");
        for(GameDto g : activeGamesList.getGamesList()) System.out.println(g + "\n");

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/newGame")
    @SendTo("/gameLobby")
    public String createNewGame(Principal principal){
        Game newGame = gameService.createNewGame(principal.getName());
        GameDto newGameDto = dtoMapper.mapToGameDto(newGame);
        activeGamesList.getGamesList().add(newGameDto);

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/joinGame/{gameId}")
    @SendTo("/gameLobby")
    public String joinGame(Principal principal, @DestinationVariable Long gameId){
        //add user to created game
        gameService.joinGameById(gameId, principal.getName());

        //add user to activeGamesList sent through webSocket
        GameDto gameDto = activeGamesList.getGamesList()
                .stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .orElse(null);
        int index = activeGamesList.getGamesList().indexOf(gameDto);
        activeGamesList.getGamesList().get(index).setPlayer2(principal.getName());
        activeGamesList.getGamesList().get(index).setGameState(GameState.READY_TO_START.name());

        //if user joined and has a created game, delete it from the list
        Game possibleCreatedGame = gameService.findGameByPlayer1Username(principal.getName());
        if(possibleCreatedGame != null){
            Long createdGameId = gameService.findGameIdByPlayer1Username(principal.getName());
            gameService.deleteGame(gameId);
            activeGamesList.getGamesList().remove(dtoMapper.mapToGameDto(possibleCreatedGame));
        }

        //for(GameDto g : activeGamesList.getGamesList()) System.out.println("listAfterJoin: "+g);
        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }

    @MessageMapping("/deleteGame")
    @SendTo("/gameLobby")
    public String deleteGameAndJoinedUserAfterUserLeft(Principal principal){

        Long gameId = gameService.findGameIdByPlayer1Username(principal.getName());
        Game createdGame = gameService.findGameByPlayer1Username(principal.getName());
        System.out.println("game id: " + gameId);

        if(gameId != null) {
//            System.out.println("deleting game " + createdGame);
            System.out.println("deleting game to dto" + dtoMapper.mapToGameDto(createdGame));
            gameService.deleteGame(gameId);
            activeGamesList.getGamesList().remove(dtoMapper.mapToGameDto(createdGame));
        }

        Game possibleGameJoined = gameService.findGameByPlayer2Username(principal.getName());
        if(possibleGameJoined != null){
            possibleGameJoined.setPlayer2(null);
            possibleGameJoined.setGameState(GameState.WAITING_FOR_PLAYERS);
            gameService.updateGameState(possibleGameJoined);

            GameDto gameDto = activeGamesList.getGamesList()
                    .stream()
                    .filter(g -> g.getPlayer2().equals(principal.getName()))
                    .findFirst()
                    .orElse(null);
            int index = activeGamesList.getGamesList().indexOf(gameDto);
            activeGamesList.getGamesList().get(index).setPlayer2("waiting for player");
            activeGamesList.getGamesList().get(index).setGameState(GameState.WAITING_FOR_PLAYERS.name());
        }

        for(GameDto g : activeGamesList.getGamesList()) System.out.println("gamesListAfterLeft: "+g);
        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }


    /*@MessageMapping("/startGame/{gameId}")
    @SendTo("/gameLobby")
    public String startGame(Principal principal, @DestinationVariable Long gameId){

        return new Gson().toJson(activeGamesList.getGamesList(), List.class);
    }*/

    @MessageMapping("/newGame/redirect")
    @SendToUser("/queue/notify")
    public void notifyUserNewGameStarted(@Payload String msg){
        GameDto gameToStart = new Gson().fromJson(msg, GameDto.class);
        System.out.println("game to start: " + gameToStart);
        messagingTemplate.convertAndSendToUser(gameToStart.getPlayer2(), "/queue/notify", msg);
        messagingTemplate.convertAndSendToUser(gameToStart.getPlayer1(), "/queue/notify", msg);
        //System.out.println("return value: " + new Gson().fromJson(msg, Map.class).get("player2").toString());
        //return new Gson().fromJson(msg, Map.class).get("player2").toString();
    }

    //------------ non-websocket controllers --------------
    @GetMapping("/")
    public String getGameLobby(Model model, Principal principal){
        List<Game> games = gameService.getAvailableGames();

        activeUsersList.getUsersList().add(principal.getName());
        List<String> loggedUsers = activeUsersList.getUsersList();
        System.out.println("list of users");
        System.out.println("length " + loggedUsers.size());
        for(String u : loggedUsers) System.out.println(u + " ");

        return "game-lobby";
    }

//    @GetMapping("/joinGame/{gameId}")
//    public ResponseEntity<Void> joinGame(Principal principal, @PathVariable Long gameId){
//        gameService.joinGameById(gameId, principal.getName());
//        GameDto gameDto = activeGamesList.getGamesList()
//                .stream()
//                .filter(g -> g.getId().equals(gameId))
//                .findFirst()
//                .orElse(null);
//        int index = activeGamesList.getGamesList().indexOf(gameDto);
//        System.out.println("index: " + index);
//
//        activeGamesList.getGamesList().get(index).setPlayer2(principal.getName());
//        for(GameDto g : activeGamesList.getGamesList()) System.out.println("listAfterJoin: "+g);
//
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}