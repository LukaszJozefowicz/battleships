package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.enums.Direction;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.exception.EntityNotFoundException;
import com.ljozefowicz.battleships.exception.RandomSetBoardFailedException;
import com.ljozefowicz.battleships.model.beans.PcShipsToAddToDB;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Ship;
import com.ljozefowicz.battleships.repository.BoardRepository;
import com.ljozefowicz.battleships.repository.ShipRepository;
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import com.ljozefowicz.battleships.stompMessageObj.ShotInfo;
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
    private PcShipsToAddToDB pcShipsToAddToDB;

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
    public Board addShipField(Board board, ShipPlacementInfo placementInfo){

        ShipUtils shipUtils = new ShipUtils();
        List<Ship> listOfShips = shipRepository.findAllByBoard_id(board.getId()).size() == 0
                ? new ArrayList<>()
                : shipRepository.findAllByBoard_id(board.getId());
        String[] fields = listOfShips.size() == 0
                ? new String[placementInfo.getLength()]
                : new Gson().fromJson(listOfShips.get(listOfShips.size() - 1).getFields(), String[].class);


        if(listOfShips.size() == 0 || shipUtils.isAllShipFieldsSet(fields)){
            shipUtils.addNewShip(board, listOfShips, placementInfo);
        } else {
            shipUtils.addShipTileToExistingShip(listOfShips, fields, placementInfo.getCoords());
        }

        board.setShips(listOfShips);
        return boardRepository.save(board);
    }

    @Override
    public String getShotResult(Board board, String coords) {

        ShipUtils shipUtils = new ShipUtils();
        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        System.out.println(Arrays.deepToString(fieldStatusArray) + "\n coords:" + coords + "\nstatus: " + fieldStatusArray[getRow(coords)][getCol(coords)].name());

        switch (fieldStatusArray[getRow(coords)][getCol(coords)]) {
            case SHIP_ALIVE:
                return shipUtils.checkIfShipIsSunk(board, coords).name();
            case EMPTY:
                return FieldStatus.MISS.name();
            default:
                throw new IllegalArgumentException("Shot should aim at a square SHIP_ALIVE or EMPTY, but was aimed at "
                                                    + fieldStatusArray[getRow(coords)][getCol(coords)]);
        }
    }

    @Override
    public ShotInfo setShotInfo(ShotInfo shotInfo, String shotResult, CurrentGameStateDto currentGameState){

        ShipUtils shipUtils = new ShipUtils();
        String sunkShipCoords = "";
        String shipFieldsToReveal = "";
        boolean isAllShipsSunk = false;

        if(shotResult.equals(FieldStatus.SHIP_SUNK.name())){
            sunkShipCoords = shipUtils.getFieldsOfShipByCoords(currentGameState.getOpponentPlayerBoard(), shotInfo.getCoords());
            isAllShipsSunk = shipUtils.checkIfAllShipsAreSunk(currentGameState.getOpponentPlayerBoard().getId());
            if(isAllShipsSunk) {
                shipFieldsToReveal = shipUtils.getShipFieldsToReveal(currentGameState.getCurrentPlayerBoard().getId());
            }
        }

        return ShotInfo.builder()
                .currentPlayer(currentGameState.getCurrentPlayer())
                .opponentPlayer(currentGameState.getOpponentPlayer())
                .shotResult(shotResult)
                .coords(shotInfo.getCoords())
                .sunkShipCoords(sunkShipCoords)
                .isAllShipsSunk(isAllShipsSunk)
                .shipFieldsToReveal(shipFieldsToReveal)
                .build();
    }

    // ----------------- Board for computer player -----------------
    @Override
    @Transactional
    public Board initializeComputerBoard() {

        BoardArrayUtils boardArrayUtils = new BoardArrayUtils();
        final Board boardToSave;
        int retry = 0;
        FieldStatus[][] fieldStatusArray = boardArrayUtils.initializeComputerBoardArray();

        while(retry < 20 && fieldStatusArray.length != 10) {    // 20 tries should be enough and not affect performance much
            fieldStatusArray = boardArrayUtils.initializeComputerBoardArray();
            retry++;
        }

        if(fieldStatusArray.length == 10) {

            boardToSave = Board.builder()
                    .persistedBoard(new Gson().toJson(fieldStatusArray))
                    .build();
            return boardRepository.save(boardToSave);
        } else throw new RandomSetBoardFailedException("Initialization of the board for computer player failed");
    }

    @Override
    public void savePcShipsToDB(Board board){
        for(ShipPlacementInfo ship : pcShipsToAddToDB.getShipsToAddToDB()){
            addShipField(board, ship);
        }
    }

    @Override
    public void clearPcShipsToAddList(){
        pcShipsToAddToDB.getShipsToAddToDB().clear();
    }

    @Override
    public String getRandomTarget(FieldStatus[][] fieldStatusArray){

        List<String> validTargetCoords = new ArrayList<>();

        for (int i = 0; i < fieldStatusArray.length; i++) {
            for (int j = 0; j < fieldStatusArray[i].length; j++) {
                if(fieldStatusArray[i][j] == FieldStatus.EMPTY || fieldStatusArray[i][j] == FieldStatus.SHIP_ALIVE){
                    validTargetCoords.add(Integer.toString(i) + j);
                }
            }
        }

        int randomCoordIndex = new Random().nextInt(validTargetCoords.size());

        return validTargetCoords.get(randomCoordIndex);
    }

    // ------------------ private ------------------

    private int getRow(String coords){
        return (int)coords.charAt(0) - 48;
    }

    private int getCol(String coords){
        return (int)coords.charAt(1) - 48;
    }

    private boolean isInArrayBounds(int coord){
        return coord < 10 && coord >= 0;
    }

    private void markNeighborByCoords(int row, int col, FieldStatus[][] fieldStatusArray, FieldStatus fieldStatusToCheck, FieldStatus fieldStatusToSet){

        EnumSet<Direction> allDirections = EnumSet.allOf(Direction.class);
        for(Direction direction : allDirections) {
            switch (direction) {
                case UP:
                    if(isInArrayBounds(row - 1)
                            && fieldStatusArray[row - 1][col] == fieldStatusToCheck){
                        fieldStatusArray[row - 1][col] = fieldStatusToSet;
                    }
                case DOWN:
                    if(isInArrayBounds(row + 1)
                            && fieldStatusArray[row + 1][col] == fieldStatusToCheck){
                        fieldStatusArray[row + 1][col] = fieldStatusToSet;
                    }
                case LEFT:
                    if(isInArrayBounds(col - 1)
                            && fieldStatusArray[row][col - 1] == fieldStatusToCheck){
                        fieldStatusArray[row][col - 1] = fieldStatusToSet;
                    }
                case RIGHT:
                    if(isInArrayBounds(col + 1)
                            && fieldStatusArray[row][col + 1] == fieldStatusToCheck){
                        fieldStatusArray[row][col + 1] = fieldStatusToSet;
                    }
                case UP_LEFT:
                    if(isInArrayBounds(row - 1)
                            && isInArrayBounds(col - 1)
                            && fieldStatusArray[row - 1][col - 1] == fieldStatusToCheck){
                        fieldStatusArray[row - 1][col - 1] = fieldStatusToSet;
                    }
                case UP_RIGHT:
                    if(isInArrayBounds(row - 1)
                            && isInArrayBounds(col + 1)
                            && fieldStatusArray[row - 1][col + 1] == fieldStatusToCheck){
                        fieldStatusArray[row - 1][col + 1] = fieldStatusToSet;
                    }
                case DOWN_LEFT:
                    if(isInArrayBounds(row + 1)
                            && isInArrayBounds(col - 1)
                            && fieldStatusArray[row + 1][col - 1] == fieldStatusToCheck){
                        fieldStatusArray[row + 1][col - 1] = fieldStatusToSet;
                    }
                case DOWN_RIGHT:
                    if(isInArrayBounds(row + 1)
                            && isInArrayBounds(col + 1)
                            && fieldStatusArray[row + 1][col + 1] == fieldStatusToCheck){
                        fieldStatusArray[row + 1][col + 1] = fieldStatusToSet;
                    }
            }
        }
    }

    // ------------------ BoardArrayUtils inner class ------------------

    private class BoardArrayUtils {

        private FieldStatus[][] initializeComputerBoardArray() {

            FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
            Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));

            List<ShipToPlaceDto> shipsToPlace = allowedShipService.getListOfShipsToPlace();

            for(ShipToPlaceDto shipToPlace : shipsToPlace){

                String coords = getRandomEmptyTile(fieldStatusArray);

                for (int i = 0; i < shipToPlace.getLength(); i++) {

                    List<Direction> possibleDirections = getDirectionsPossible(fieldStatusArray, coords);

                    if(possibleDirections.isEmpty()) {
                        pcShipsToAddToDB.getShipsToAddToDB().clear();
                        return new FieldStatus[][]{{FieldStatus.EMPTY}};
                    } else {

                        pcShipsToAddToDB.getShipsToAddToDB().add(ShipPlacementInfo.builder()
                                .fieldStatus("SHIP_ALIVE")
                                .type(shipToPlace.getType())
                                .length(shipToPlace.getLength())
                                .coords(coords)
                                .build());

                        fieldStatusArray[getRow(coords)][getCol(coords)] = FieldStatus.SHIP_ALIVE;
                        coords = getNextCoords(coords, possibleDirections);
                    }
                }
                markFieldsAroundPlacedShip(fieldStatusArray, FieldStatus.EMPTY, FieldStatus.AROUND_SHIP);
            }

            fieldStatusArray = setAroundShipToEmpty(fieldStatusArray);
            return fieldStatusArray;
        }

        private String getRandomEmptyTile(FieldStatus[][] fieldStatusArray){

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

        private String getNextCoords(String coords, List<Direction> possibleDirections){

            Direction randomDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
            switch (randomDirection){
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

        private void markFieldsAroundPlacedShip(FieldStatus[][] fieldStatusArray,FieldStatus fieldStatusToCheck ,FieldStatus fieldStatusToSet){

            for (int i = 0; i < fieldStatusArray.length; i++) {
                for (int j = 0; j < fieldStatusArray[i].length; j++) {
                    if(fieldStatusArray[i][j] == FieldStatus.SHIP_ALIVE){
                        markNeighborByCoords(i, j, fieldStatusArray, fieldStatusToCheck, fieldStatusToSet);
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
    }

    // ------------------ ShipUtils inner class ------------------

    private class ShipUtils {

        private boolean isAllShipFieldsSet(String[] fields){
            return !Arrays.asList(fields).contains("null");
        }

        private void addNewShip(Board board, List<Ship> listOfShips, ShipPlacementInfo placementInfo){

            String[] fields = new String[placementInfo.getLength()];
            Arrays.fill(fields, "null");
            fields[0] = placementInfo.getCoords();

            listOfShips.add(Ship.builder()
                    .type(placementInfo.getType())
                    .length(placementInfo.getLength())
                    .fields(new Gson().toJson(fields))
                    .isDestroyed(false)
                    .board(board)
                    .build());
        }

        private void addShipTileToExistingShip(List<Ship> listOfShips, String[] fields, String coords){

            for(int i = 0; i < fields.length; i++){
                if("null".equals(fields[i])) {
                    fields[i] = coords;
                    break;
                }
            }
            listOfShips.get(listOfShips.size() - 1).setFields(new Gson().toJson(fields));
        }

        private FieldStatus checkIfShipIsSunk(Board board, String coords){

            Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%")
                    .orElseThrow(() -> new EntityNotFoundException("Ship with coords: " + coords + " not found in db"));

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
                            markNeighborByCoords(i, j, fieldStatusArray, FieldStatus.EMPTY, FieldStatus.AROUND_SUNK);
                        }
                    }
                }
            }
        }

        private String getFieldsOfShipByCoords(Board board, String coords){
            Ship ship = shipRepository.findByBoard_idAndFieldsLike(board.getId(), "%" + coords + "%")
                    .orElseThrow(() -> new EntityNotFoundException("Ship with coords: " + coords + "not found in db"));
            return ship.getFields();
        }

        private boolean checkIfAllShipsAreSunk(Long boardId){
            return shipRepository.findAllByBoard_id(boardId)
                    .stream()
                    .allMatch(Ship::getIsDestroyed);
        }

        private String getShipFieldsToReveal(Long boardId){
            List<Ship> ships = shipRepository.findAllByBoard_id(boardId);
            String[] fieldsToReveal = ships.stream()
                    .filter(s -> !s.getIsDestroyed())
                    .flatMap(ship -> Arrays.stream(new Gson().fromJson(ship.getFields(), String[].class)))
                    .toArray(String[]::new);

            return new Gson().toJson(fieldsToReveal);
        }
    }

    //    private Long countShipPlacedTiles(FieldStatus[][] fieldStatusArray){
//        return Arrays.stream(fieldStatusArray)
//                .flatMap(Stream::of)
//                .filter(field -> field == FieldStatus.SHIP_ALIVE)
//                .count();
//
//    }

    /*public static void main(String[] args) {

        FieldStatus[][] fieldStatusArray = new FieldStatus[][]{
                {FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE},
                {FieldStatus.AROUND_SHIP, FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.EMPTY},
                {FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE, FieldStatus.SHIP_ALIVE},
                {FieldStatus.EMPTY, FieldStatus.AROUND_SHIP, FieldStatus.MISS, FieldStatus.AROUND_SHIP}
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
