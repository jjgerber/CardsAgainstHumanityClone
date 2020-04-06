package org.j3y.cards.model.gameplay;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Player {
    private String playerId;
    private String name;
    @JsonIgnore private Game currentGame;
    private Set<Phrase> phrases;
    private int score;

    @JsonIgnore private Semaphore mutex;

    public Player() {
        super();
        this.mutex = new Semaphore(1);
        this.phrases = new HashSet<>();
        this.score = 0;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public String getCurrentGameName() {
        return currentGame == null ? null : currentGame.getName();
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public Set<Phrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(Set<Phrase> phrases) {
        this.phrases = phrases;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore() {
        this.score++;
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }
}
