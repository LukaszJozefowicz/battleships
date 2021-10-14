package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.dto.CurrentGameStateDto;
import com.ljozefowicz.battleships.enums.ShipShape;
import com.ljozefowicz.battleships.exception.RandomShotException;
import com.ljozefowicz.battleships.stompMessageObj.ShipPlacementInfo;
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
import com.ljozefowicz.battleships.stompMessageObj.ShotInfo;
import com.ljozefowicz.battleships.util.Field;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final ShipRepository shipRepository;
    private final AllowedShipService allowedShipService;

    @Override
    @Transactional
    public Board initializeEmptyBoard() {
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
    @Transactional
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
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Board updateField(Board board, String coords, FieldStatus fieldStatus) {
        FieldStatus[][] fieldStatusArray = getBoardAsArray(board);
        fieldStatusArray[getRow(coords)][getCol(coords)] = fieldStatus;
        board.setPersistedBoard(new Gson().toJson(fieldStatusArray));

        return boardRepository.save(board);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Board addShipField(Board board, ShipPlacementInfo placementInfo){

        ShipUtils shipUtils = new ShipUtils();
        List<Ship> listOfShips = shipRepository.findAllByBoard_idOrderByBoard_idAscIdAsc(board.getId()).size() == 0
                ? new ArrayList<>()
                : shipRepository.findAllByBoard_idOrderByBoard_idAscIdAsc(board.getId());
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
        //System.out.println(Arrays.deepToString(fieldStatusArray) + "\n coords:" + coords + "\nstatus: " + fieldStatusArray[getRow(coords)][getCol(coords)].name());

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
    public ShotInfo getShotInfo(ShotInfo shotInfo, String shotResult, CurrentGameStateDto currentGameState){

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

    @Override
    @Transactional
    public Board autoInitializeBoard(ShipShape shipShape, Board existingBoard) {

        BoardArrayUtils boardArrayUtils = new BoardArrayUtils();
        final Board boardToSave;
        int retry = 0;
        List<ShipPlacementInfo> shipsToAdd = new ArrayList<>();
        FieldStatus[][] fieldStatusArray = boardArrayUtils.autoInitializeBoardArray(shipShape, shipsToAdd);

        while(retry < 50 && fieldStatusArray.length != 10) {    // 50 tries should be enough and not affect performance much
            System.out.println("---------------------------retry---------------------------: " + retry);
            fieldStatusArray = boardArrayUtils.autoInitializeBoardArray(shipShape, shipsToAdd);
            retry++;
        }

        if(fieldStatusArray.length == 10) {
            if(existingBoard == null){
                boardToSave = Board.builder()
                    .persistedBoard(new Gson().toJson(fieldStatusArray))
                    .build();
            } else {
                existingBoard.setPersistedBoard(new Gson().toJson(fieldStatusArray));
                boardToSave = existingBoard;
            }

            for(ShipPlacementInfo ship : shipsToAdd){
                addShipField(boardToSave, ship);
            }

            return boardRepository.save(boardToSave);
        } else throw new RandomSetBoardFailedException("Initialization of the board for computer player failed");
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

    @Override
    public String getRandomTargetPossiblyNearShipHit(FieldStatus[][] fieldStatusArray, ShipShape shipShape){

        BoardArrayUtils boardArrayUtils = new BoardArrayUtils();
        List<String> shipHitCoords = boardArrayUtils.getShipHitTiles(fieldStatusArray);

        if (shipHitCoords.isEmpty()) return getRandomTarget(fieldStatusArray);

        if(shipShape == ShipShape.CLASSIC){
            List<String> validTargets = boardArrayUtils.getTargetsIfSureOfDirection(fieldStatusArray, shipHitCoords);
            if(!validTargets.isEmpty()) {
//                System.out.println("sure of direction");
                return validTargets.get(new Random().nextInt(validTargets.size()));
            }
        }

        for(String coords : shipHitCoords){
            Predicate <FieldStatus> isStatusEmptyOrAlive = f -> f == FieldStatus.EMPTY || f == FieldStatus.SHIP_ALIVE;
            List<Direction> possibleDirections = boardArrayUtils.getDirectionsPossible(fieldStatusArray, coords, isStatusEmptyOrAlive);

            if(possibleDirections.isEmpty()) continue;

            return boardArrayUtils.getNextCoords(coords, possibleDirections, Direction.NO_LOCKED_DIRECTION);
        }
        throw new RandomShotException("Something went wrong when choosing random shot coords");
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

        BiPredicate<Integer, Integer> isInArrayBounds = (aRow, aCol) -> aRow < 10 && aRow >= 0 && aCol < 10 && aCol >= 0;
        BiPredicate<Integer, Integer> isFieldStatusToCheck = (aRow, aCol) -> fieldStatusArray[aRow][aCol] == fieldStatusToCheck;
        EnumSet<Direction> allDirections = EnumSet.allOf(Direction.class);

        for(Direction direction : allDirections) {
            switch (direction) {

                case UP:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row - 1, col)){
                        fieldStatusArray[row - 1][col] = fieldStatusToSet;
                    }
                case DOWN:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row + 1, col)){
                        fieldStatusArray[row + 1][col] = fieldStatusToSet;
                    }
                case LEFT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row, col - 1)){
                        fieldStatusArray[row][col - 1] = fieldStatusToSet;
                    }
                case RIGHT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row, col + 1)){
                        fieldStatusArray[row][col + 1] = fieldStatusToSet;
                    }
                case UP_LEFT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row - 1, col - 1)){
                        fieldStatusArray[row - 1][col - 1] = fieldStatusToSet;
                    }
                case UP_RIGHT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row - 1, col + 1)){
                        fieldStatusArray[row - 1][col + 1] = fieldStatusToSet;
                    }
                case DOWN_LEFT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row + 1, col - 1)){
                        fieldStatusArray[row + 1][col - 1] = fieldStatusToSet;
                    }
                case DOWN_RIGHT:
                    if(isInArrayBounds.and(isFieldStatusToCheck).test(row + 1, col + 1)){
                        fieldStatusArray[row + 1][col + 1] = fieldStatusToSet;
                    }
            }
        }
    }

    // ------------------ BoardArrayUtils inner class ------------------

    private class BoardArrayUtils {

        private FieldStatus[][] autoInitializeBoardArray(ShipShape shipShape, List<ShipPlacementInfo> shipsToAdd) {

            shipsToAdd.clear();
            FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
            Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));

            List<ShipToPlaceDto> shipsToPlace = allowedShipService.getListOfShipsToPlace();
            Direction lockedDirection = Direction.NO_LOCKED_DIRECTION;

            for(ShipToPlaceDto shipToPlace : shipsToPlace){

                System.out.println("Ship: " + shipToPlace.getType());
//                String coords = getRandomEmptyTile(fieldStatusArray);
                Field initialField = getRandomEmptyTileValidForShip(fieldStatusArray, shipToPlace.getLength());
                String coords = initialField.getCoords();
                if(shipShape == ShipShape.CLASSIC){
                    lockedDirection = initialField.getDirection();
                }

                for (int whichShipField = 0; whichShipField < shipToPlace.getLength(); whichShipField++) {
                    System.out.println("Tile: " + whichShipField);
                    System.out.println("locked direction: " + lockedDirection);
                    System.out.println("coords: " + coords);
                    Predicate<FieldStatus> isStatusEmpty = f -> f == FieldStatus.EMPTY;

                    List<Direction> possibleDirections = getDirectionsPossible(fieldStatusArray, coords, isStatusEmpty);

                    switch (shipShape) {
                        case ANY_DIRECTION:
                            if (possibleDirections.isEmpty()) {
                                shipsToAdd.clear();
                                return new FieldStatus[][]{{FieldStatus.EMPTY}};
                            }
                            break;
                        case CLASSIC:
                            if(lockedDirection == Direction.NO_LOCKED_DIRECTION){
                                shipsToAdd.clear();
                                return new FieldStatus[][]{{FieldStatus.EMPTY}};
                            }
                            break;
                    }
//                    if(shipShape == ShipShape.CLASSIC && whichShipField == 0){
//
//                        lockedDirection = getLockedDirection(fieldStatusArray, coords, shipToPlace.getLength());
//                        if(lockedDirection == Direction.NO_LOCKED_DIRECTION){
//                            shipsToAdd.clear();
//                            return new FieldStatus[][]{{FieldStatus.EMPTY}};
//                        }
//
//                    }
//                    if(shipShape == ShipShape.CLASSIC && whichShipField == shipToPlace.getLength() - 1) {
//                        lockedDirection = Direction.NO_LOCKED_DIRECTION;
//                    }

                    shipsToAdd.add(ShipPlacementInfo.builder()
                                .fieldStatus("SHIP_ALIVE")
                                .type(shipToPlace.getType())
                                .length(shipToPlace.getLength())
                                .coords(coords)
                                .build());

                    fieldStatusArray[getRow(coords)][getCol(coords)] = FieldStatus.SHIP_ALIVE;
                    coords = getNextCoords(coords, possibleDirections, lockedDirection);
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

            if(!isAnyEmptyTile) throw new RandomSetBoardFailedException("There are no empty tiles to place a ship on");

            while(true) {
                int row = new Random().nextInt(10);
                int col = new Random().nextInt(10);
                if(fieldStatusArray[row][col] == FieldStatus.EMPTY)
                    return Integer.toString(row) + col;
            }
        }

        private Field getRandomEmptyTileValidForShip(FieldStatus[][] fieldStatusArray, int shipLength){

            boolean isAnyEmptyTile = Arrays.stream(fieldStatusArray)
                    .flatMap(Stream::of) //arr -> Arrays.stream(arr)
                    .anyMatch(fieldStatus -> fieldStatus == FieldStatus.EMPTY);

            if(!isAnyEmptyTile) throw new RandomSetBoardFailedException("There are no empty tiles to place a ship on");

            int retry = 0;

            while(retry < 100) {
                System.out.println("retry getRandomTile: " + retry);
                retry++;
                int row = new Random().nextInt(10);
                int col = new Random().nextInt(10);
                Direction randomValidDirection = getLockedDirection(fieldStatusArray, Integer.toString(row) + col, shipLength);
                if(fieldStatusArray[row][col] == FieldStatus.EMPTY && randomValidDirection != Direction.NO_LOCKED_DIRECTION)
                    return Field.builder()
                        .coords(Integer.toString(row) + col)
                        .direction(randomValidDirection)
                        .build();
            }
            throw new RandomSetBoardFailedException("Can't find a suitable place for the ship. Try lowering amount of allowed ships in settings.");
        }

        private List<String> getShipHitTiles(FieldStatus[][] fieldStatusArray){

            List<String> shipHitCoordsList = new ArrayList<>();

            for (int i = 0; i < fieldStatusArray.length; i++) {
                for (int j = 0; j < fieldStatusArray[i].length; j++) {
                    if(fieldStatusArray[i][j] == FieldStatus.SHIP_HIT){
                        shipHitCoordsList.add(Integer.toString(i) + j);
                    }
                }
            }
            return shipHitCoordsList;
        }

        private List<String> getTargetsIfSureOfDirection(FieldStatus[][] fieldStatusArray, List<String> shipHitCoords){

            BiPredicate<Integer, Integer> isNextToEachOther = (coord1, coord2) -> coord1 + 1 == coord2;
            BiPredicate<Integer, Integer> isTheSame = (coord1, coord2) -> coord1 == coord2;
            BiPredicate<Integer, Integer> isInArrayBounds = (aRow, aCol) -> aRow < 10 && aRow >= 0 && aCol < 10 && aCol >= 0;
            BiPredicate<Integer, Integer> isEmptyOrAlive = (aRow, aCol) ->
                    fieldStatusArray[aRow][aCol] == FieldStatus.EMPTY || fieldStatusArray[aRow][aCol] == FieldStatus.SHIP_ALIVE;

            List<String> validTargets = new ArrayList<>();

            for (int i = 1; i < shipHitCoords.size(); i++) {

                int previousRow = getRow(shipHitCoords.get(i - 1));
                int previousCol = getCol(shipHitCoords.get(i - 1));
                int currentRow = getRow(shipHitCoords.get(i));
                int currentCol = getCol(shipHitCoords.get(i));

                if(isNextToEachOther.test(previousRow, currentRow) && isTheSame.test(previousCol, currentCol)) {
                    if(isInArrayBounds.and(isEmptyOrAlive).test(currentRow + 1, currentCol))
                        validTargets.add(Integer.toString(currentRow + 1) + currentCol);
                    if(isInArrayBounds.and(isEmptyOrAlive).test(previousRow - 1, previousCol))
                        validTargets.add(Integer.toString(previousRow - 1) + previousCol);
                }

                if(isTheSame.test(previousRow, currentRow) && isNextToEachOther.test(previousCol, currentCol)) {
                    if(isInArrayBounds.and(isEmptyOrAlive).test(currentRow, currentCol + 1))
                        validTargets.add(currentRow + Integer.toString(currentCol + 1));
                    if(isInArrayBounds.and(isEmptyOrAlive).test(previousRow, previousCol - 1))
                        validTargets.add(previousRow + Integer.toString(previousCol - 1));
                }
            }

            //System.out.println("validTargets.size(): " + validTargets.size() + " isEmpty: " + validTargets.isEmpty());
            return validTargets;
        }

        private List<Direction> getDirectionsPossible(FieldStatus[][] fieldStatusArray, String coords, Predicate fieldStatusCheck){

            int row = getRow(coords);
            int col = getCol(coords);
            List<Direction> directionsPossible = new ArrayList<>();

            if(!isInArrayBounds(row) || !isInArrayBounds(col)) return directionsPossible;

            if (isInArrayBounds(row - 1) && fieldStatusCheck.test(fieldStatusArray[row - 1][col])) {
                directionsPossible.add(Direction.UP);
            }
            if (isInArrayBounds(row + 1) && fieldStatusCheck.test(fieldStatusArray[row + 1][col])) {
                directionsPossible.add(Direction.DOWN);
            }
            if (isInArrayBounds(col - 1) && fieldStatusCheck.test(fieldStatusArray[row][col - 1])) {
                directionsPossible.add(Direction.LEFT);
            }
            if (isInArrayBounds(col + 1) && fieldStatusCheck.test(fieldStatusArray[row][col + 1])) {
                directionsPossible.add(Direction.RIGHT);
            }
            return directionsPossible;
        }

        private Direction getLockedDirection(FieldStatus[][] fieldStatusArray, String coords, int shipLength){

            int row = getRow(coords);
            int col = getCol(coords);

            List<Direction> directionsPossible = new ArrayList<>(List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));

            for(int i = 0; i < shipLength; i++){
                if(!isInArrayBounds(row - shipLength) ||
                        (fieldStatusArray[row - i][col] != FieldStatus.EMPTY && directionsPossible.contains(Direction.UP)))
                    directionsPossible.remove(Direction.UP);
                if(!isInArrayBounds(row + shipLength) ||
                        (fieldStatusArray[row + i][col] != FieldStatus.EMPTY  && directionsPossible.contains(Direction.DOWN)))
                    directionsPossible.remove(Direction.DOWN);
                if(!isInArrayBounds(col - shipLength) ||
                        (fieldStatusArray[row][col - i] != FieldStatus.EMPTY && directionsPossible.contains(Direction.LEFT)))
                    directionsPossible.remove(Direction.LEFT);
                if(!isInArrayBounds(col + shipLength) ||
                        (fieldStatusArray[row][col + i] != FieldStatus.EMPTY && directionsPossible.contains(Direction.RIGHT)))
                    directionsPossible.remove(Direction.RIGHT);
            }

            System.out.println("getLockedDirection row: " + row + " col: " + col + " shipLength: " + shipLength);
            System.out.println("possibleDirections: " + Arrays.toString(directionsPossible.toArray()));
//            if(isInArrayBounds(row - shipLength)){
//                for(int i = 0; i < shipLength; i++){
//                    if(!fieldStatusCheck.test(fieldStatusArray[row - i][col]))
//                        break;
//                    if(i == shipLength - 1)
//                        directionsPossible.add(Direction.UP);
//                }
//            }
//
//            if(isInArrayBounds(row + shipLength)){
//                for(int i = 0; i < shipLength; i++){
//                    if(!fieldStatusCheck.test(fieldStatusArray[row + i][col]))
//                        break;
//                    if(i == shipLength - 1)
//                        directionsPossible.add(Direction.DOWN);
//                }
//            }
//
//            if(isInArrayBounds(col - shipLength)){
//                for(int i = 0; i < shipLength; i++){
//                    if(!fieldStatusCheck.test(fieldStatusArray[row][col - i]))
//                        break;
//                    if(i == shipLength - 1)
//                        directionsPossible.add(Direction.LEFT);
//                }
//            }
//
//            if(isInArrayBounds(col + shipLength)){
//                for(int i = 0; i < shipLength; i++){
//                    if(!fieldStatusCheck.test(fieldStatusArray[row][col + i]))
//                        break;
//                    if(i == shipLength - 1)
//                        directionsPossible.add(Direction.RIGHT);
//                }
//            }

            if(!directionsPossible.isEmpty()) {
                return directionsPossible.get(new Random().nextInt(directionsPossible.size()));
            } else {
                return Direction.NO_LOCKED_DIRECTION;
            }

        }

        private Direction addDirection(FieldStatus[][] fieldStatusArray, List<Direction> directionsPossible, String coords, int shipLength, Direction direction){

            int row = getRow(coords);
            int col = getCol(coords);
            int coord;

            switch(direction){
                case UP:
                    shipLength *= -1;
                    coord = row;
            }

            if(isInArrayBounds(row - shipLength)){
                for(int i = 0; i < shipLength; i++){
                    if(fieldStatusArray[row - i][col] != FieldStatus.EMPTY){
                        break;
                    }
                    if(i == shipLength - 1){
                        directionsPossible.add(Direction.UP);
                    }
                }
            }
            return Direction.NO_LOCKED_DIRECTION;
        }

        private String getNextCoords(String coords, List<Direction> possibleDirections, Direction lockedDirection){

//            Direction randomDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));

            final Direction randomDirection;

            if(lockedDirection != Direction.NO_LOCKED_DIRECTION) {
                randomDirection = lockedDirection;
            } else {
                randomDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
            }

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

//        FieldStatus[][] fieldStatusArray = new FieldStatus[][]{
//                {FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE},
//                {FieldStatus.AROUND_SHIP, FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.EMPTY},
//                {FieldStatus.SHIP_ALIVE, FieldStatus.EMPTY, FieldStatus.SHIP_ALIVE, FieldStatus.SHIP_ALIVE},
//                {FieldStatus.EMPTY, FieldStatus.AROUND_SHIP, FieldStatus.MISS, FieldStatus.AROUND_SHIP}
//        };
//        System.out.println(Arrays.deepToString(fieldStatusArray));
//
//        FieldStatus[][] arr = Arrays.stream(fieldStatusArray)
//                .map(Stream::of)
//                .map(str -> str
//                        .map(FieldStatus::setEmptyIfAroundShip)
//                        .toArray(FieldStatus[]::new))
//                .toArray(FieldStatus[][]::new);
//
//        System.out.println(Arrays.deepToString(arr));
//        System.out.println(Arrays.stream(fieldStatusArray)
//                .flatMap(Stream::of)
//                .filter(field -> field == FieldStatus.SHIP_ALIVE)
//                .count());
        for (int i = 0; i < 1000; i++) {


            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            int random = new Random().nextInt(list.size());
            Set<Integer> set = list.stream()
                    .collect(Collectors.toSet());

            Integer result = set.stream()
                    .skip(random)
                    .findFirst()
                    .get();
            System.out.println("random nb to skip: " + random + " result: " + result);
        }
    }*/
}
