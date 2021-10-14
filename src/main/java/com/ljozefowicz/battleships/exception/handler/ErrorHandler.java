package com.ljozefowicz.battleships.exception.handler;

import com.ljozefowicz.battleships.exception.RandomSetBoardFailedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(RandomSetBoardFailedException.class)
        public String handleSetBoardFailureException(Exception exception){

        return "setBoardError";
//        return "redirect:/setBoardError";
    }
}
