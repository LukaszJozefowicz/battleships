package com.ljozefowicz.battleships.stompMessageObj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Message {
    private String messageType;
    private String username;
    private String message;
}
