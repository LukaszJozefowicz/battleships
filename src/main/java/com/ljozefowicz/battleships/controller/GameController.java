package com.ljozefowicz.battleships.controller;

import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
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
@RequestMapping("/setup")
public class GameController {

    BoardService boardService;
    AllowedShipService allowedShipService;

//    @GetMapping
//    public String showBoard(Model model){
//        Board board = new Board();
//        model.addAttribute("board", board.getCellsList());
//        model.addAttribute("counter", new Counter(0));
//        return "index";
//    }

    @GetMapping("/placeShips")
    public ResponseEntity<Void> placeShips(@RequestParam (required = false) String coords, Model model){

        System.out.println(coords);

//        Board board = boardService.initializeBoard();
//        model.addAttribute("board", boardService.getFieldsList(board));
//        model.addAttribute("counter", new Counter(0));

        Board board = boardService.getBoardById(1L);
        System.out.println(board.getPersistedBoard());
        boardService.updateField(board, coords, FieldStatus.SHIP_ALIVE);
        //model.addAttribute("board", boardService.getFieldsList(board));
        //model.addAttribute("counter", new Counter(10));
        model.addAttribute("shipsToPlace", allowedShipService.getListOfShipsToPlace());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        //return "fragments/board :: board";
    }
}
