package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.Field;
import com.ljozefowicz.battleships.model.entity.Board;

import java.util.List;

public interface BoardService {
    Board initializeBoard();
    Board updateField(Board board, String coords, FieldStatus fieldStatus);
    List<List<Field>> getFieldsList(Board board);
    Board resetBoard(Board board);
    Board getBoardById(Long id);
}
