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
import com.ljozefowicz.battleships.service.AllowedShipService;
import com.ljozefowicz.battleships.service.BoardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService{

    private BoardRepository boardRepository;
    private AllowedShipRepository allowedShipRepository;
    private ShipRepository shipRepository;

    @Override
    public Board initializeBoard() {
        FieldStatus[][] fieldStatusArray = new FieldStatus[10][10];
        Arrays.stream(fieldStatusArray).forEach(e -> Arrays.fill(e, FieldStatus.EMPTY));
        Board boardToSave = Board.builder()
                .id(1L)
                .persistedBoard(new Gson().toJson(fieldStatusArray))
                .ships(initializeListOfShips(this.getBoardById(1L)))
                .build();
        return boardRepository.save(boardToSave);
    }

    @Override
    public Board updateField(Board board, String coords, FieldStatus fieldStatus) {

        List<List<Field>> cellsList = getFieldsList(board);
        final int col = (int)coords.charAt(0) - 48, //48 is digit 0 in ascii
                  row = (int)coords.charAt(1) - 48;
        System.out.println("col = " + col + " row = " + row);
        final Field updatedField = Field.builder()
                .fieldStatus(fieldStatus)
                .coords(coords)
                .x(col)
                .y(row)
                .build();
        System.out.println("updated field = " + updatedField.toString());
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

        FieldStatus[][] fieldStatusArray = list.stream()
                .map(e -> e.stream()
                        .map(Field::getFieldStatus)
                        .toArray(FieldStatus[]::new))
                .toArray(FieldStatus[][]::new);

        return fieldStatusArray;
    }

//    private int getSumOfShipFields() {
//        return allowedShipRepository.findAll().stream()
//                .reduce(0, (subtotal, element) -> subtotal + element.getNumberOfAllowed(), Integer::sum);
//    }

    private List<Ship> initializeListOfShips(Board board){
        List<Ship> listOfShips = new ArrayList<>();
        List<AllowedShip> listOfAllowedShips = allowedShipRepository.findAll();

        for(AllowedShip allowedShip : listOfAllowedShips){
            for (int i = 0; i < allowedShip.getNumberOfAllowed(); i++) {
                listOfShips.add(Ship.builder()
                        .type(allowedShip.getType())
                        .length(allowedShip.getLength())
                        .board(board)
                        .build());
            }
        }
        return listOfShips;

    }

 }
