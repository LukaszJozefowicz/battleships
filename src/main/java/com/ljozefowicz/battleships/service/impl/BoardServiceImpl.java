package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Ship;
import com.ljozefowicz.battleships.repository.BoardRepository;
import com.ljozefowicz.battleships.repository.ShipRepository;
import com.ljozefowicz.battleships.service.BoardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final ShipRepository shipRepository;

    @Override
    public Board initializeBoard() {
        FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));
        Board boardToSave = Board.builder()
                .persistedBoard(new Gson().toJson(fieldStatusArray))
                .build();
        return boardRepository.save(boardToSave);
    }

    @Override
    public FieldStatus[][] getBoardAsArray(Board board){
        return new Gson().fromJson(board.getPersistedBoard(), FieldStatus[][].class);
    }

    @Override
    public void deleteBoard(Long boardId){
        boardRepository.deleteById(boardId);
    }

    @Override
    @Transactional
    public Board resetBoard(Board board){
        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));
        board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
        shipRepository.deleteAllByBoard_id(board.getId());
        return boardRepository.save(board);
    }

    @Override
    @Transactional
    public Board updateField(Board board, String coords, FieldStatus fieldStatus) {
        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        fieldStatusArray[getRow(coords)][getCol(coords)] = fieldStatus;
        board.setPersistedBoard(new Gson().toJson(fieldStatusArray));

        return boardRepository.save(board);
    }

    @Override
    public String getShotResult(Board board, String coords) {

        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);

        switch (fieldStatusArray[getRow(coords)][getCol(coords)]) {
            case SHIP_ALIVE:
                return FieldStatus.SHIP_HIT.name();
            case EMPTY:
                return FieldStatus.MISS.name();
            default:
                return "";
        }
    }

    @Override
    @Transactional
    public Board addShipField(Board board, String type, int shipLength, String coords){
        List<Ship> listOfShips = shipRepository.findAllByBoard_id(board.getId()).size() == 0
                ? new ArrayList<>()
                : shipRepository.findAllByBoard_id(board.getId());
        String[] fields = listOfShips.size() > 0
                ? new Gson().fromJson(listOfShips.get(listOfShips.size() - 1).getFields(), String[].class)
                : new String[shipLength];

        if(listOfShips.size() == 0 || isAllShipFieldsSet(fields)){

            fields = new String[shipLength];
            Arrays.fill(fields, "null");
            fields[0] = coords;

            listOfShips.add(Ship.builder()
                    .type(type)
                    .length(shipLength)
                    .fields(new Gson().toJson(fields))
                    .isDestroyed(false)
                    .board(board)
                    .build());
        } else {

            for(int i = 0; i < fields.length; i++){
                if("null".equals(fields[i])) {
                    fields[i] = coords;
                    break;
                }
            }
        }

        listOfShips.get(listOfShips.size() - 1).setFields(new Gson().toJson(fields));
        board.setShips(listOfShips);

        return boardRepository.save(board);
    }

    @Override
    @Transactional
    public boolean checkIfShipIsSunk(Board board, String coords){

        Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%")
                .orElseThrow(() -> new EntityNotFoundException("Ship with coords: " + coords + "not found in db"));

        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        String[] shipFields = new Gson().fromJson(ship.getFields(), String[].class);

        boolean isShipSunk = Arrays.stream(shipFields)
                .allMatch(field -> fieldStatusArray[getRow(field)][getCol(field)] != FieldStatus.SHIP_ALIVE);

        if(isShipSunk){
            ship.setIsDestroyed(true);
            shipRepository.save(ship);

            setShipFieldsSunkOnBoard(fieldStatusArray, shipFields);
            board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
            boardRepository.save(board);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIfAllShipsAreSunk(Long boardId){
        return shipRepository.findAllByBoard_id(boardId)
                .stream()
                .allMatch(Ship::getIsDestroyed);
    }

    @Override
    public String getFieldsOfShipByCoords(Board board, String coords){
        Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%")
                .orElseThrow(() -> new EntityNotFoundException("Ship with coords: " + coords + "not found in db"));
        return ship.getFields();
    }

    @Override
    public String getShipFieldsToReveal(Long boardId){
        List<Ship> ships = shipRepository.findAllByBoard_id(boardId);
        String[] fieldsToReveal = ships.stream()
                .filter(s -> !s.getIsDestroyed())
                .flatMap(ship -> Arrays.stream(new Gson().fromJson(ship.getFields(), String[].class)))
                .toArray(String[]::new);

        return new Gson().toJson(fieldsToReveal);
    }

    private boolean isAllShipFieldsSet(String[] fields){
        return !Arrays.asList(fields).contains("null");
    }

    private int getRow(String coords){
        return (int)coords.charAt(0) - 48;
    }

    private int getCol(String coords){
        return (int)coords.charAt(1) - 48;
    }

    private void setShipFieldsSunkOnBoard(FieldStatus[][] fieldStatusArray, String[] shipFields){
        for (int i = 0; i < fieldStatusArray.length; i++) {
            for (int j = 0; j < fieldStatusArray[i].length; j++) {
                for (String field : shipFields) {
                    if (i == getRow(field) && j == getCol(field)) {
                        fieldStatusArray[i][j] = FieldStatus.SHIP_SUNK;
                    }
                }
            }
        }
    }
}
