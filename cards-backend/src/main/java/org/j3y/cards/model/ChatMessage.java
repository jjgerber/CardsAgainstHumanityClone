package org.j3y.cards.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ChatMessage {
    String playerId;
    String playerName;
    String message;
    ZonedDateTime messageTime = ZonedDateTime.now(ZoneOffset.UTC);

    public ChatMessage(String playerId, String playerName, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.message = message;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getMessageTime() {
        return messageTime;
    }

}
