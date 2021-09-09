package com.ljozefowicz.battleships.model.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ActiveUsersList {

    private List<String> usersList;

    public ActiveUsersList(){
        usersList = new ArrayList<>();
        usersList.add("usersList");
    }
}
