package com.ljozefowicz.battleships.controller;

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

import java.util.List;


@Controller
@AllArgsConstructor
@RequestMapping("/")
public class GameController {

    GameService gameService;
    BoardService boardService;
    AllowedShipService allowedShipService;

    @GetMapping("/newGame")
    public String createNewGame(Model model){
        Game game = gameService.createNewGame();
        model.addAttribute("createdGame", game);
        List<Game> games = gameService.getAvailableGames();
        model.addAttribute("games", games);
        return "game-lobby";
        //return "new-game";
    }

    @GetMapping("/availableGames")
    public String getAvailableGames(Model model){
        List<Game> games = gameService.getAvailableGames();
        model.addAttribute("games", games);
        return "join-game";
    }

    @GetMapping("/joinGame")
    public String joinGame(@RequestParam (required = false) long gameId, Model model){
        Game game = gameService.joinGameById(gameId);
        model.addAttribute("game", game);
        return "new-game";
    }

    @GetMapping("/placeShips")
    public ResponseEntity<Void> placeShips(@RequestParam (required = false) String coords, Model model){

        System.out.println(coords);

//        Board board = boardService.initializeBoard();
//        model.addAttribute("board", boardService.getFieldsList(board));
//        model.addAttribute("counter", new Counter(0));
        Game game = gameService.findGameByLoggedInUsername();
        Board board = game.getFirstPlayerBoard();
        //System.out.println(board.getPersistedBoard());
        boardService.updateField(board, coords, FieldStatus.SHIP_ALIVE);
        model.addAttribute("board", boardService.getFieldsList(board));
        model.addAttribute("counter", new Counter(10));
        model.addAttribute("shipsToPlace", allowedShipService.getListOfShipsToPlace());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        return "fragments/board :: board";
    }

    @GetMapping("/finishGame")
    public ResponseEntity<Void> finishGame(){
        Long gameId = gameService.findGameIdByLoggedInUsername();
        gameService.deleteGame(gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
