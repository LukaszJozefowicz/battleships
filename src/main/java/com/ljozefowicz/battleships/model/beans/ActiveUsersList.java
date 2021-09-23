package com.ljozefowicz.battleships.model.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Data
public class ActiveUsersList {

    private Set<String> usersList;

    public ActiveUsersList(){
        usersList = new HashSet<>();
        usersList.add("usersList");     //a little trick so I can check in javascript what kind of objects are passed
    }
}
