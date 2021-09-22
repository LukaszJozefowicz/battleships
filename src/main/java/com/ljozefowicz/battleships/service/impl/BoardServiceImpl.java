package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.util.Field;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Ship;
import com.ljozefowicz.battleships.repository.AllowedShipRepository;
import com.ljozefowicz.battleships.repository.BoardRepository;
import com.ljozefowicz.battleships.repository.ShipRepository;
import com.ljozefowicz.battleships.service.BoardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final AllowedShipRepository allowedShipRepository;
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
    public void deleteBoard(Long boardId){
        boardRepository.deleteById(boardId);
    }

    @Override
    @Transactional
    public Board resetBoard(Board board){
        FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));
        board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
        shipRepository.deleteAllByBoard_id(board.getId());
        return boardRepository.save(board);
    }

    @Override
    @Transactional
    public Board updateField(Board board, String coords, FieldStatus fieldStatus) {

        List<List<Field>> cellsList = getFieldsList(board);
        final int row = (int)coords.charAt(0) - 48, //48 is digit 0 in ascii
                  col = (int)coords.charAt(1) - 48;

        final Field updatedField = Field.builder()
                .fieldStatus(fieldStatus)
                .coords(coords)
                .x(row)
                .y(col)
                .build();

        cellsList.get(row).set(col, updatedField);
        board.setPersistedBoard(new Gson().toJson(listOfFieldsToArray2D(cellsList)));


        return boardRepository.save(board);
    }

    @Override
    public String getShotResult(Board board, String coords) {

        FieldStatus[][] statusArray = new Gson().fromJson(board.getPersistedBoard(), FieldStatus[][].class);

        //System.out.println("statusArray field: " + statusArray[getRow(coords)][getCol(coords)]);


        switch (statusArray[getRow(coords)][getCol(coords)]) {
            case SHIP_ALIVE:
                return FieldStatus.SHIP_HIT.name();
            case EMPTY:
                return FieldStatus.MISS.name();
            default:
                return "";

        }
    }

    @Override
    public List<List<Field>> getFieldsList(Board board) {
        FieldStatus[][] fieldStatusArray = new Gson().fromJson(board.getPersistedBoard(), FieldStatus[][].class);
        List<List<Field>> cellsList = new ArrayList<>();
        for (int i = 0; i < fieldStatusArray.length; i++) {
            List<Field> listRow = new ArrayList<>();
            cellsList.add(listRow);
            for (int j = 0; j < fieldStatusArray[i].length; j++) {
                listRow.add(Field.builder()
                        .fieldStatus(fieldStatusArray[i][j])
                        .coords(String.valueOf((char)(i+65)) + (j+1))
                        .x(i)
                        .y(j)
                        .build());
            }
        }

        return cellsList;
    }

    @Override
    public Board getBoardById(Long id){
        return boardRepository.findById(id).orElse(null);
    }

    private FieldStatus[][] listOfFieldsToArray2D(List<List<Field>> list){

        return list.stream()
                .map(e -> e.stream()
                        .map(Field::getFieldStatus)
                        .toArray(FieldStatus[]::new))
                .toArray(FieldStatus[][]::new);
    }

    private List<Ship> initializeListOfShips(){
        List<Ship> listOfShips = new ArrayList<>();
        List<AllowedShip> listOfAllowedShips = allowedShipRepository.findAll();

        for(AllowedShip allowedShip : listOfAllowedShips){
            for (int i = 0; i < allowedShip.getNumberOfAllowed(); i++) {
                listOfShips.add(Ship.builder()
                        .type(allowedShip.getType())
                        .length(allowedShip.getLength())
                        .isDestroyed(false)
                        .build());
            }
        }
        return listOfShips;

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

        Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%");
        System.out.println("found ship: " + Ship.builder()
                                            .id(ship.getId())
                                            .board(null)
                                            .type(ship.getType())
                                            .length(ship.getLength())
                                            .fields(ship.getFields())
                                            .isDestroyed(ship.getIsDestroyed())
                                            .build());
        System.out.println("coords: " + coords);
        FieldStatus[][] fieldStatusArray = new Gson().fromJson(board.getPersistedBoard(), FieldStatus[][].class);
        String[] fields = new Gson().fromJson(ship.getFields(), String[].class);

        String[] fieldsShipNotHit = Arrays.stream(fields)
                .filter(field -> fieldStatusArray[getRow(field)][getCol(field)] != FieldStatus.SHIP_HIT)
                .toArray(String[]::new);

        if(fieldsShipNotHit.length == 0){
            ship.setIsDestroyed(true);
            shipRepository.save(ship);

            System.out.println("ship after save: " + Ship.builder()
                    .id(ship.getId())
                    .board(null)
                    .type(ship.getType())
                    .length(ship.getLength())
                    .fields(ship.getFields())
                    .isDestroyed(ship.getIsDestroyed())
                    .build());

            for (int i = 0; i < fieldStatusArray.length; i++) {
                for (int j = 0; j < fieldStatusArray[i].length; j++) {
                    for (int k = 0; k < fields.length; k++) {
                        if(i == getRow(fields[k]) && j == getCol(fields[k])){
                            fieldStatusArray[i][j] = FieldStatus.SHIP_SUNK;
                        }
                    }
                }
            }

            board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
            boardRepository.save(board);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIfAllShipsAreSunk(Long boardId){

        List<Ship> shipsAlive = shipRepository.findAllByBoard_id(boardId)
                .stream()
                .filter(s -> !s.getIsDestroyed())
                .collect(Collectors.toList());
        return shipsAlive.size() <= 0;
    }

    @Override
    public String getFieldsOfShipByCoords(Board board, String coords){
        Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%");
        return ship.getFields();
    }

    @Override
    public String getShipFieldsToReveal(Long boardId){
//        FieldStatus[][] statusArray = new Gson().fromJson(board.getPersistedBoard(), FieldStatus[][].class);
//        String[] fieldsToReveal = Arrays.stream(statusArray)
//                .flatMap(arr -> Arrays.stream(arr))
//                .toArray(String[]::new);
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

    /*public static void main(String[] args) {

                List<Ship> ships = List.of( Ship.builder()
                                            .fields("[\"00\",\"10\",\"20\",\"30\"]")
                                            .isDestroyed(false)
                                            .build(),
                                            Ship.builder()
                                            .fields("[\"03\",\"13\",\"23\",\"33\"]")
                                            .isDestroyed(false)
                                            .build(),
                                            Ship.builder()
                                            .fields("[\"03\",\"13\",\"23\",\"33\"]")
                                            .isDestroyed(true)
                                            .build());
        String[] fieldsToReveal =ships.stream()
                        .filter(s -> !s.getIsDestroyed())
                        //.flatMap(ship -> Arrays.stream(Arrays.stream(new Gson().fromJson(ship.getFields(), String[].class)).toArray()))
                        .flatMap(ship -> Arrays.stream(new Gson().fromJson(ship.getFields(), String[].class)))
                        .toArray(String[]::new);
        System.out.println("length: " + fieldsToReveal.length);
        for(String s : fieldsToReveal){
            System.out.println(s + " ");
        }
    }*/
}
