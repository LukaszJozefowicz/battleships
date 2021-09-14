package com.ljozefowicz.battleships.service.impl;

import com.google.gson.Gson;
import com.ljozefowicz.battleships.enums.FieldStatus;
import com.ljozefowicz.battleships.model.Field;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.model.entity.Board;
import com.ljozefowicz.battleships.model.entity.Ship;
import com.ljozefowicz.battleships.repository.AllowedShipRepository;
import com.ljozefowicz.battleships.repository.BoardRepository;
import com.ljozefowicz.battleships.repository.ShipRepository;
import com.ljozefowicz.battleships.service.BoardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                //.id(id)
                .persistedBoard(new Gson().toJson(fieldStatusArray))
                //.ships(initializeListOfShips())
                .build();
        //boardToSave.getShips().forEach(e -> e.setBoard(boardToSave));
        return boardRepository.save(boardToSave);
    }

    @Override
    public Board resetBoard(Board board){
        FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));
        board.setPersistedBoard(new Gson().toJson(fieldStatusArray));
        return boardRepository.save(board);
    }

    @Override
    public Board updateField(Board board, String coords, FieldStatus fieldStatus) {

        List<List<Field>> cellsList = getFieldsList(board);
        final int col = (int)coords.charAt(0) - 48, //48 is digit 0 in ascii
                  row = (int)coords.charAt(1) - 48;

        final Field updatedField = Field.builder()
                .fieldStatus(fieldStatus)
                .coords(coords)
                .x(col)
                .y(row)
                .build();

        cellsList.get(row).set(col, updatedField);
        board.setPersistedBoard(new Gson().toJson(listOfFieldsToArray2D(cellsList)));


        return boardRepository.save(board);
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
    public Board saveShipField(Board board, String type, int shipLength, String coords){
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
            //Ship shipToUpdate = listOfShips.get(listOfShips.size() - 1);
            //fields = new Gson().fromJson(shipToUpdate.getFields(), String[].class);

            for(int i = 0; i < fields.length; i++){
                if("null".equals(fields[i])) {
                    fields[i] = coords;
                    break;
                }
            }
//            listOfShips.get(listOfShips.size() - 1).setFields(new Gson().toJson(fields));
            //listOfShips.set(listOfShips.size() - 1, shipToUpdate);
//            board.setShips(listOfShips);
        }
        listOfShips.get(listOfShips.size() - 1).setFields(new Gson().toJson(fields));
        board.setShips(listOfShips);

        return boardRepository.save(board);
    }

    private boolean isAllShipFieldsSet(String[] fields){
        return !Arrays.asList(fields).contains("null");
    }

    /*public static void main(String[] args) {
        String[] fields = new String[4];
        Arrays.fill(fields, "null");
        fields[0] = "97";
//        fields = Arrays.stream(fields)
//                .map(s -> s == null ? "null" : s)
//                .toArray(String[]::new);

        String ships = new Gson().toJson(fields);
        System.out.println(ships);

        String[] fieldsAfterConvert = new Gson().fromJson(ships, String[].class);
        for(int i = 0; i < fieldsAfterConvert.length; i++){
            System.out.println("ele: "+ fieldsAfterConvert[i] + " bool: " + fieldsAfterConvert[i].equals("null"));
            if("null".equals(fieldsAfterConvert[i])) {
                System.out.println("dupa");
                fieldsAfterConvert[i] = "65";
                break;
            }
        }

        System.out.println(new Gson().toJson(fieldsAfterConvert));
    }*/
}
