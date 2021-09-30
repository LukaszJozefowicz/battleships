package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.entity.Board;

public interface BoardService {
    Board initializeBoard();
    Board initializeComputerBoard();
    String getRandomEmptyTile(FieldStatus[][] fieldStatusArray);
    void deleteBoard(Long id);
    Board updateField(Board board, String coords, FieldStatus fieldStatus);
    FieldStatus[][] getBoardAsArray(Board board);
    Board resetBoard(Board board);
    Board addShipField(Board board, String type, int shipLength, String coords);
    String getShotResult(Board board, String coords);
    String getFieldsOfShipByCoords(Board board, String coords);
    boolean checkIfAllShipsAreSunk(Long boardId);
    String getShipFieldsToReveal(Long boardId);
}
