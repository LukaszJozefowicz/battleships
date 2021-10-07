package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.entity.Board;

public interface BoardService {
    Board initializeBoard();
    FieldStatus[][] getBoardAsArray(Board board);
    void deleteBoard(Long id);
    Board resetBoard(Board board);

    Board updateField(Board board, String coords, FieldStatus fieldStatus);
    Board addShipField(Board board, ShipPlacementInfo placementInfo);
    String getShotResult(Board board, String coords);
    String getFieldsOfShipByCoords(Board board, String coords);
    boolean checkIfAllShipsAreSunk(Long boardId);
    String getShipFieldsToReveal(Long boardId);

    Board initializeComputerBoard();
    String getRandomTarget(FieldStatus[][] fieldStatusArray);
    void savePcShipsToDB(Board board);
    void clearPcShipsToAddList();
}
