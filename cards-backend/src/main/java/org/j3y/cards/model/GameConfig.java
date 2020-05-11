package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonView;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

public class GameConfig {
    @JsonView(Views.Limited.class)
    @Size(min = 1)
    private List<String> deckIds;

    @JsonView(Views.Limited.class)
    @Size(min = 1)
    private List<String> deckNames;

    @JsonView(Views.Limited.class)
    @Min(15)
    @Max(120)
    private int turnTimeout;

    @JsonView(Views.Limited.class)
    @Min(3)
    @Max(10)
    private int maxPlayers;

    @JsonView(Views.Limited.class)
    @Min(1)
    @Max(15)
    private int maxScore;

    public List<String> getDeckIds() {
        return deckIds;
    }

    public void setDeckIds(List<String> deckIds) {
        this.deckIds = deckIds;
    }

    public List<String> getDeckNames() {
        return deckNames;
    }

    public void setDeckNames(List<String> deckNames) {
        this.deckNames = deckNames;
    }

    public int getTurnTimeout() {
        return turnTimeout;
    }

    public void setTurnTimeout(int turnTimeout) {
        this.turnTimeout = turnTimeout;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
}
