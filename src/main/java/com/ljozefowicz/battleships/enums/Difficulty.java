package com.ljozefowicz.battleships.enums;

public enum Difficulty {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    private final String difficulty;

    Difficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getNameCamelCase() {
        return difficulty;
    }
}
