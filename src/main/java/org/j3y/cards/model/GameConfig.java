package org.j3y.cards.model;

import java.util.List;

public class GameConfig {
    private List<String> deckIds;
    private Integer turnTimeout;
    private Integer maxPlayers;
    private Integer maxScore;
    private Integer winningWaitTimeMillis;

    public List<String> getDeckIds() {
        return deckIds;
    }

    public void setDeckIds(List<String> deckIds) {
        this.deckIds = deckIds;
    }

    public Integer getTurnTimeout() {
        return turnTimeout;
    }

    public void setTurnTimeout(Integer turnTimeout) {
        this.turnTimeout = turnTimeout;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getWinningWaitTimeMillis() {
        return winningWaitTimeMillis;
    }

    public void setWinningWaitTimeMillis(Integer winningWaitTimeMillis) {
        this.winningWaitTimeMillis = winningWaitTimeMillis;
    }
}
