package com.ljozefowicz.battleships.service;

public interface LoggedInService {

    String findLoggedInUsername();

    void autoLogin(String username, String password);
}
