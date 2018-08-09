package org.j3y.cards.model.gameplay;

import java.util.Set;
import java.util.concurrent.Semaphore;

public class Player {
    private String name;
    private Game currentGame;
    private Set<Phrase> phrases;
    private Semaphore mutex;

    public Player() {
        super();
        this.mutex = new Semaphore(1);
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

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public Set<Phrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(Set<Phrase> phrases) {
        this.phrases = phrases;
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }
}
