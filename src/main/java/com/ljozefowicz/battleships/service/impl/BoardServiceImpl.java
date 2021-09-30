package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.enums.Direction;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
import com.ljozefowicz.battleships.exception.RandomSetBoardFailedException;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Ship;
import com.ljozefowicz.battleships.repository.BoardRepository;
import com.ljozefowicz.battleships.repository.ShipRepository;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final ShipRepository shipRepository;
    private final AllowedShipService allowedShipService;

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
    public String getShotResult(Board board, String coords) {

        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);

        switch (fieldStatusArray[getRow(coords)][getCol(coords)]) {
            case SHIP_ALIVE:
                return checkIfShipIsSunk(board, coords).name();
            case EMPTY:
                return FieldStatus.MISS.name();
            default:
                throw new IllegalArgumentException("Shot should aim at a square SHIP_ALIVE or EMPTY, but was aimed at "
                                                    + fieldStatusArray[getRow(coords)][getCol(coords)]);
        }
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

    @Transactional
    private FieldStatus checkIfShipIsSunk(Board board, String coords){

        Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%")
                .orElseThrow(() -> new EntityNotFoundException("Ship with coords: " + coords + "not found in db"));

        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        String[] shipFields = new Gson().fromJson(ship.getFields(), String[].class);

        boolean isShipSunk = Arrays.stream(shipFields)
                .filter(field -> !field.equals(coords))
                .allMatch(field -> fieldStatusArray[getRow(field)][getCol(field)] != FieldStatus.SHIP_ALIVE);

        if(isShipSunk){
            ship.setIsDestroyed(true);
            shipRepository.save(ship);

            setShipFieldsSunkOnBoard(fieldStatusArray, shipFields);
            board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
            boardRepository.save(board);
            return FieldStatus.SHIP_SUNK;
        }
        return FieldStatus.SHIP_HIT;
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

    // ----------------- initialize board for computer player
    @Override
    public Board initializeComputerBoard() {

        final Board boardToSave;
        int retry = 0;
        FieldStatus[][] fieldStatusArray = initializeComputerBoardArray();
        //System.out.println("before while retry " + retry + " arrayLength: " + fieldStatusArray.length);

        while(retry < 20 && fieldStatusArray.length != 10) {
            fieldStatusArray = initializeComputerBoardArray();
            System.out.println("retry " + retry + " arrayLength: " + fieldStatusArray.length);
            retry++;
        }

        if(fieldStatusArray.length == 10) {
            //System.out.println("Saving pc board");

            boardToSave = Board.builder()
                    .persistedBoard(new Gson().toJson(fieldStatusArray))
                    .build();
            return boardRepository.save(boardToSave);
        } else throw new RandomSetBoardFailedException("Initialization of the board for computer player failed");
    }

    @Override
    public String getRandomEmptyTile(FieldStatus[][] fieldStatusArray){

        boolean isAnyEmptyTile = Arrays.stream(fieldStatusArray)
                .flatMap(Stream::of) //arr -> Arrays.stream(arr)
                .anyMatch(fieldStatus -> fieldStatus == FieldStatus.EMPTY);

        if(!isAnyEmptyTile) return "error";

        while(true) {
            int row = new Random().nextInt(10);
            int col = new Random().nextInt(10);
            if(fieldStatusArray[row][col] == FieldStatus.EMPTY)
                return Integer.toString(row) + col;
        }
    }

    private FieldStatus[][] initializeComputerBoardArray() {

        FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));

        List<ShipToPlaceDto> shipsToPlace = allowedShipService.getListOfShipsToPlace();

        for(ShipToPlaceDto shipToPlace : shipsToPlace){

            String coords = getRandomEmptyTile(fieldStatusArray);

            for (int i = 0; i < shipToPlace.getLength(); i++) {

                List<Direction> possibleDirections = getDirectionsPossible(fieldStatusArray, coords);
                if(possibleDirections.size() == 0) {
                    FieldStatus[][] dummyArray = {{FieldStatus.EMPTY}};
                    return dummyArray;
                }
                Direction randomDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
                fieldStatusArray[getRow(coords)][getCol(coords)] = FieldStatus.SHIP_ALIVE;
                coords = getNextCoords(coords, randomDirection);

            }
            markFieldsAroundPlacedShip(fieldStatusArray);
        }

        fieldStatusArray = setAroundShipToEmpty(fieldStatusArray);
        return fieldStatusArray;
    }

    private List<Direction> getDirectionsPossible(FieldStatus[][] fieldStatusArray, String coords){
        int row = getRow(coords);
        int col = getCol(coords);

        List<Direction> directionsPossible = new ArrayList<>();

        if(isInArrayBounds(row - 1) && fieldStatusArray[row - 1][col] == FieldStatus.EMPTY){
            directionsPossible.add(Direction.UP);
        }
        if(isInArrayBounds(row + 1) && fieldStatusArray[row + 1][col] == FieldStatus.EMPTY){
            directionsPossible.add(Direction.DOWN);
        }
        if(isInArrayBounds(col - 1) && fieldStatusArray[row][col - 1] == FieldStatus.EMPTY){
            directionsPossible.add(Direction.LEFT);
        }
        if(isInArrayBounds(col + 1) && fieldStatusArray[row][col + 1] == FieldStatus.EMPTY){
            directionsPossible.add(Direction.RIGHT);
        }
        return directionsPossible;
    }

    private String getNextCoords(String coords, Direction direction){
        switch (direction){
            case UP:
                return Integer.toString(getRow(coords) - 1) + getCol(coords);
            case DOWN:
                return Integer.toString(getRow(coords) + 1) + getCol(coords);
            case LEFT:
                return getRow(coords) + Integer.toString(getCol(coords) - 1);
            case RIGHT:
                return getRow(coords) + Integer.toString(getCol(coords) + 1);
            default:
                return "error";
        }
    }

    private void markFieldsAroundPlacedShip(FieldStatus[][] fieldStatusArray){
        for (int i = 0; i < fieldStatusArray.length; i++) {
            for (int j = 0; j < fieldStatusArray[i].length; j++) {
                if(fieldStatusArray[i][j] == FieldStatus.SHIP_ALIVE){
                    markNeighborOfPlacedShip(i, j, fieldStatusArray);
                }
            }
        }
    }

    private void markNeighborOfPlacedShip(int row, int col, FieldStatus[][] fieldStatusArray){
        EnumSet<Direction> allDirections = EnumSet.allOf(Direction.class);
        for(Direction direction : allDirections) {
            switch (direction) {
                case UP:
                    if(isInArrayBounds(row - 1)
                            && fieldStatusArray[row - 1][col] == FieldStatus.EMPTY){
                        fieldStatusArray[row - 1][col] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case DOWN:
                    if(isInArrayBounds(row + 1)
                            && fieldStatusArray[row + 1][col] == FieldStatus.EMPTY){
                        fieldStatusArray[row + 1][col] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case LEFT:
                    if(isInArrayBounds(col - 1)
                            && fieldStatusArray[row][col - 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row][col - 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case RIGHT:
                    if(isInArrayBounds(col + 1)
                            && fieldStatusArray[row][col + 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row][col + 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case UP_LEFT:
                    if(isInArrayBounds(row - 1)
                            && isInArrayBounds(col - 1)
                            && fieldStatusArray[row - 1][col - 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row - 1][col - 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case UP_RIGHT:
                    if(isInArrayBounds(row - 1)
                            && isInArrayBounds(col + 1)
                            && fieldStatusArray[row - 1][col + 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row - 1][col + 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case DOWN_LEFT:
                    if(isInArrayBounds(row + 1)
                            && isInArrayBounds(col - 1)
                            && fieldStatusArray[row + 1][col - 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row + 1][col - 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
                case DOWN_RIGHT:
                    if(isInArrayBounds(row + 1)
                            && isInArrayBounds(col + 1)
                            && fieldStatusArray[row + 1][col + 1] == FieldStatus.EMPTY){
                        fieldStatusArray[row + 1][col + 1] = FieldStatus.AROUND_PLACED_SHIP;
                    }
            }
        }
    }

    private FieldStatus[][] setAroundShipToEmpty(FieldStatus[][] fieldStatusArray){

        return Arrays.stream(fieldStatusArray)
                .map(Stream::of)
                .map(str -> str
                        .map(FieldStatus::setEmptyIfAroundShip)
                        .toArray(FieldStatus[]::new))
                .toArray(FieldStatus[][]::new);
    }

//    private Long countShipPlacedTiles(FieldStatus[][] fieldStatusArray){
//        return Arrays.stream(fieldStatusArray)
//                .flatMap(Stream::of)
//                .filter(field -> field == FieldStatus.SHIP_ALIVE)
//                .count();
//
//    }

    private boolean isInArrayBounds(int coord){
        return coord < 10 && coord >= 0;
    }

    /*public static void main(String[] args) {

        FieldStatus[][] fieldStatusArray = {
                {FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE},
                {FieldStatus.AROUND_PLACED_SHIP, FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.EMPTY},
                {FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE, FieldStatus.SHIP_ALIVE},
                {FieldStatus.EMPTY, FieldStatus.AROUND_PLACED_SHIP, FieldStatus.MISS, FieldStatus.AROUND_PLACED_SHIP}
        };
        System.out.println(Arrays.deepToString(fieldStatusArray));

        FieldStatus[][] arr = Arrays.stream(fieldStatusArray)
                .map(Stream::of)
                .map(str -> str
                        .map(FieldStatus::setEmptyIfAroundShip)
                        .toArray(FieldStatus[]::new))
                .toArray(FieldStatus[][]::new);

        System.out.println(Arrays.deepToString(arr));
        System.out.println(Arrays.stream(fieldStatusArray)
                .flatMap(Stream::of)
                .filter(field -> field == FieldStatus.SHIP_ALIVE)
                .count());
    }*/
}
