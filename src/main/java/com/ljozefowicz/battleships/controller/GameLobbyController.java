package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ljozefowicz.battleships.dto.LoggedInUserDto;
import com.ljozefowicz.battleships.dto.MessageDto;
import com.ljozefowicz.battleships.model.ActiveUsersList;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
//@RequestMapping("/ws")
@AllArgsConstructor
public class GameLobbyController {

    GameService gameService;
    SessionRegistry sessionRegistry;
    ActiveUsersList activeUsersList;


    @MessageMapping("/listOfUsers")
    @SendTo("/gameLobby")
    public String getListOfActiveUsers(){
//        return LoggedInUserDto.builder()
//                .username(user.getUsername())
//                .build();
        /*List<String> loggedUsers = sessionRegistry.getAllPrincipals()
                .stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .filter(principal -> principal instanceof UserDetails)
                .map(UserDetails.class::cast)
                .map(userDetails -> userDetails.getUsername())
                .collect(Collectors.toList());*/
        List<String> loggedUsers = activeUsersList.getUsersList();
        return new Gson().toJson(loggedUsers, List.class);
    }

    @MessageMapping("/userLeft")
    @SendTo("/gameLobby")
    public String sendUsersListAfterUserLeft(String jsonList){
        System.out.println("user left json list string: " + jsonList);
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
        List<String> listFromJson = new Gson().fromJson(jsonList, listType);
        System.out.println("java list");
        for (String s : listFromJson){
            System.out.println(s + " ");
        }
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

    @MessageMapping("/activeUser")
    @SendTo("/gameLobby")
    public void getActiveUserName(LoggedInUserDto user){
        LoggedInUserDto.builder()
                .username(user.getUsername())
                .build();
    }

    @GetMapping("/gameLobby")
    public String getGameLobby(Model model, Principal principal){
        List<Game> games = gameService.getAvailableGames();
        //List<String> loggedUsers = loggedInUserStore.getUsers();



//        List<String> loggedUsers = sessionRegistry.getAllPrincipals()
//                .stream()
//                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
//                .filter(principal -> principal instanceof UserDetails)
//                .map(UserDetails.class::cast)
//                .map(userDetails -> userDetails.getUsername())
//                .collect(Collectors.toList());
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        activeUsersList.getUsersList().add(principal.getName());
        List<String> loggedUsers = activeUsersList.getUsersList();
        System.out.println("list of users");
        System.out.println("length " + loggedUsers.size());
        for(String u : loggedUsers) System.out.println(u + " ");

        model.addAttribute("games", games);
        model.addAttribute("loggedUsers", loggedUsers);
        return "game-lobby";
    }
}
