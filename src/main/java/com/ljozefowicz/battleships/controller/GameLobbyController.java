package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.LoggedInUserDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
//@RequestMapping("/ws")
@AllArgsConstructor
public class GameLobbyController {

    GameService gameService;
    SessionRegistry sessionRegistry;

    @MessageMapping("/listOfUsers")
    @SendTo("/gameLobby")
    public String getListOfActiveUsers(){
//        return LoggedInUserDto.builder()
//                .username(user.getUsername())
//                .build();
        List<String> loggedUsers = sessionRegistry.getAllPrincipals()
                .stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .filter(principal -> principal instanceof UserDetails)
                .map(UserDetails.class::cast)
                .map(userDetails -> userDetails.getUsername())
                .collect(Collectors.toList());
        return new Gson().toJson(loggedUsers, List.class);
    }

    @MessageMapping("/userLeft")
    @SendTo("/gameLobby")
    public String sendUsersListAfterUserLeft(String jsonList){
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

    @MessageMapping("/activeUser")
    @SendTo("/gameLobby")
    public void getActiveUserName(LoggedInUserDto user){
        LoggedInUserDto.builder()
                .username(user.getUsername())
                .build();
    }

    @GetMapping("/gameLobby")
    public String getGameLobby(Model model){
        List<Game> games = gameService.getAvailableGames();
        //List<String> loggedUsers = loggedInUserStore.getUsers();



        List<String> loggedUsers = sessionRegistry.getAllPrincipals()
                .stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .filter(principal -> principal instanceof UserDetails)
                .map(UserDetails.class::cast)
                .map(userDetails -> userDetails.getUsername())
                .collect(Collectors.toList());

        System.out.println("list of users");
        System.out.println("length " + loggedUsers.size());
        for(String u : loggedUsers) System.out.println(u + " ");

        model.addAttribute("games", games);
        model.addAttribute("loggedUsers", loggedUsers);
        return "game-lobby";
    }
}
