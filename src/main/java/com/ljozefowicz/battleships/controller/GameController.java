package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.GameService;
import com.ljozefowicz.battleships.util.Counter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@Controller
@AllArgsConstructor
@RequestMapping("/")
public class GameController {

    GameService gameService;
    BoardService boardService;
    AllowedShipService allowedShipService;

    @GetMapping("/newGame/{gameId}")
    public String createNewGame(Model model, @PathVariable Long gameId, Principal principal){
        Game game = gameService.findGameById(gameId);
        model.addAttribute("board", boardService.getFieldsList(game.getFirstPlayerBoard()));
        model.addAttribute("counter", new Counter(0));
        model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
        return "index";
    }

    @GetMapping("/availableGames")
    public String getAvailableGames(Model model){
        List<Game> games = gameService.getAvailableGames();
        model.addAttribute("games", games);
        return "join-game";
    }

//    @GetMapping("/joinGame")
//    public String joinGame(@RequestParam (required = false) long gameId, Model model){
//        Game game = gameService.joinGameById(gameId);
//        model.addAttribute("game", game);
//        return "new-game";
//    }

    @GetMapping("/placeShips")
    public ResponseEntity<Void> placeShips(@RequestParam String coords, Model model, Principal principal){

        System.out.println(coords);

        Game game = gameService.findGameByPlayer1Username(principal.getName());
        if(game == null)
            game = gameService.findGameByPlayer2Username(principal.getName());

        Board board = game.getFirstPlayerBoard();
        if(principal.getName().equals(game.getPlayer2().getUsername()))
            board = game.getSecondPlayerBoard();


        boardService.updateField(board, coords, FieldStatus.SHIP_ALIVE);
        model.addAttribute("board", boardService.getFieldsList(board));
        model.addAttribute("counter", new Counter(10));
        model.addAttribute("shipsToPlace", allowedShipService.getListOfShipsToPlace());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/finishGame")
    public ResponseEntity<Void> finishGame(Principal principal){
        Long gameId = gameService.findGameIdByPlayer1Username(principal.getName());
        gameService.deleteGame(gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
