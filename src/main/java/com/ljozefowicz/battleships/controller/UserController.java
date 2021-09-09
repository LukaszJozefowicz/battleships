package com.ljozefowicz.battleships.controller;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.service.UserService;
import com.ljozefowicz.battleships.util.Counter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final BoardService boardService;
    private final AllowedShipService allowedShipService;


    @GetMapping("/login")
    public String login(){
        return "login";
    }

    /*@GetMapping("/")
    public String getHomePage(Model model){
//        Board board = boardService.initializeBoard(1L);
//        model.addAttribute("board", boardService.getFieldsList(board));
//        model.addAttribute("counter", new Counter(0));
//        model.addAttribute("shipsToPlace", new Gson().toJson(allowedShipService.getListOfShipsToPlace()));
        return "index";
    }*/
}
