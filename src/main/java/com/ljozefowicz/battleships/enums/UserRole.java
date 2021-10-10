package com.ljozefowicz.battleships.enums;

import java.util.List;

public enum UserRole {

    ROLE_USER(1L, "USER"),
    ROLE_ADMIN(2L, "ADMIN"),
    ROLE_BOT_EASY(3L, "BotEasy"),
    ROLE_BOT_NORMAL(4L, "BotNormal"),
    ROLE_BOT_HARD(5L, "BotHard");


    private final Long id;
    private final String roleName;
    private static final List<String> botNames = List.of("BotEasy", "BotNormal", "BotHard");;

    UserRole(Long id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public static boolean isBot(String name){
        return botNames.stream()
                .anyMatch(botName -> name.contains(botName));
    }
}
