package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.dto.ShipToPlaceDto;
import com.ljozefowicz.battleships.model.entity.AllowedShip;
import com.ljozefowicz.battleships.repository.AllowedShipRepository;
import com.ljozefowicz.battleships.service.AllowedShipService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AllowedShipServiceImpl implements AllowedShipService {

    private final AllowedShipRepository allowedShipRepository;
    private final DtoMapper dtoMapper;

    @Override
    public List<ShipToPlaceDto> getListOfShipsToPlace() {

        List<ShipToPlaceDto> resultList = new ArrayList<>();
        List<AllowedShip> allowedShipsFromDb = allowedShipRepository.findAll();

        for(AllowedShip allowedShip : allowedShipsFromDb){
            for (int i = 0; i < allowedShip.getNumberOfAllowed(); i++) {
                resultList.add(dtoMapper.mapToShipToPlaceDto(allowedShip, i+1));
            }
        }

        return resultList;
    }
}
