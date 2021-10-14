package com.ljozefowicz.battleships.enums;

import java.util.EnumSet;

public enum UserRole {

    ROLE_USER(1L),
    ROLE_ADMIN(2L),
    ROLE_BOT(3L);

    private final Long id;

    UserRole(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static boolean isBot(String name){
        boolean isNameContainsDifficulty = EnumSet.allOf(Difficulty.class)
                .stream()
                .anyMatch(difficulty -> name.contains(difficulty.getNameCamelCase()));

        return name.contains("Bot") && isNameContainsDifficulty;
    }

}
