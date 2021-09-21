package com.ljozefowicz.battleships.model.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ActiveUsersList {

    private List<String> usersList;

    public ActiveUsersList(){
        usersList = new ArrayList<>();
        usersList.add("usersList");     //a little trick so I can check in javascript what kind of objects are passed
    }
}
