package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Player implements Authentication {

    @JsonView(Views.Limited.class)
    private String playerName;

    @JsonView(Views.Limited.class)
    private String name;

    @JsonIgnore private Game currentGame;

    @JsonView(Views.LoggedInUser.class)
    private Set<Phrase> phrases;

    @JsonView(Views.LoggedInUser.class)
    private List<Phrase> selectedPhrases;

    @JsonView(Views.Limited.class)
    private int score;

    @JsonIgnore private Semaphore mutex;

    public Player() {
        this.name = UUID.randomUUID().toString();
        this.mutex = new Semaphore(1);
        this.phrases = new HashSet<>();
        this.selectedPhrases = new ArrayList<>();
        this.score = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getName() {
        return name;
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

    @JsonView(Views.Limited.class)
    public String getCurrentGameUuid() {
        return this.currentGame == null ? null : this.currentGame.getUuid();
    }

    public Set<Phrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(Set<Phrase> phrases) {
        this.phrases = phrases;
    }

    public List<Phrase> getSelectedPhrases() {
        return selectedPhrases;
    }

    public void setSelectedPhrases(List<Phrase> selectedPhrases) {
        this.selectedPhrases = selectedPhrases;
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

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    @JsonIgnore
    public Object getCredentials() {
        return null;
    }

    @Override
    @JsonIgnore
    public Object getDetails() {
        return null;
    }

    @Override
    @JsonIgnore
    public Object getPrincipal() {
        return null;
    }

    @Override
    @JsonIgnore
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return name.equals(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
