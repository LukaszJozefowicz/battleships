package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.util.Field;
import com.ljozefowicz.battleships.model.entity.Board;

import java.util.List;

public interface BoardService {
    Board initializeBoard();
    void deleteBoard(Long id);
    Board updateField(Board board, String coords, FieldStatus fieldStatus);
    List<List<Field>> getFieldsList(Board board);
    Board resetBoard(Board board);
    Board addShipField(Board board, String type, int shipLength, String coords);
    String getShotResult(Board board, String coords);
    boolean checkIfShipIsSunk(Board board, String coords);
    String getFieldsOfShipByCoords(Board board, String coords);
    boolean checkIfAllShipsAreSunk(Long boardId);
    String getShipFieldsToReveal(Long boardId);
//    Ship getShipByField(Long boardId, String field);
    Board getBoardById(Long id);
}
