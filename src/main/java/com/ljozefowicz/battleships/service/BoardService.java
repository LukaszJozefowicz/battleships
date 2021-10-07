package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.stompMessageObj.ShotInfo;

public interface BoardService {
    Board initializeBoard();
    void deleteBoard(Long id);
    Board resetBoard(Board board);
    FieldStatus[][] getBoardAsArray(Board board);

    Board updateField(Board board, String coords, FieldStatus fieldStatus);
    Board addShipField(Board board, ShipPlacementInfo placementInfo);
    String getShotResult(Board board, String coords);
    ShotInfo setShotInfo(ShotInfo shotInfo, String shotResult, CurrentGameStateDto currentGameState);

    Board initializeComputerBoard();
    String getRandomTarget(FieldStatus[][] fieldStatusArray);
    void savePcShipsToDB(Board board);
    void clearPcShipsToAddList();
}
