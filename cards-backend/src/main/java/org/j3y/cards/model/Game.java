package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Game {
    @JsonView(Views.Limited.class) private final String uuid;
    @JsonView(Views.Limited.class) private String name;
    @JsonView(Views.Limited.class) private Player owner;
    @JsonIgnore private Set<Card> cardSet;
    @JsonIgnore private Set<Phrase> phraseSet;

    @JsonView(Views.Full.class) private List<Player> players;

    @JsonView(Views.Limited.class) private GameConfig gameConfig;
    
    @JsonIgnore private Semaphore mutex;
    @JsonView(Views.Full.class) private Player judgingPlayer;
    @JsonView(Views.Full.class) private Card currentCard;

    @JsonView(Views.Limited.class) private GameState gameState;
    @JsonView(Views.Limited.class) private ZonedDateTime gameStateTime;
    @JsonView(Views.Limited.class) private ZonedDateTime gameTimeoutTime;
    @JsonView(Views.Judging.class) private List<List<Phrase>> phraseSelections;

    @JsonView(Views.Full.class) private Integer judgeChoiceWinner;
    @JsonView(Views.Full.class) private Player lastWinningPlayer;

    public Game() {
        this.uuid = UUID.randomUUID().toString();
        this.mutex = new Semaphore(1);
        this.phraseSelections = new ArrayList<>();
        this.setGameState(GameState.LOBBY);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Set<Card> getCardSet() {
        return cardSet;
    }

    public void setCardSet(Set<Card> cardSet) {
        this.cardSet = cardSet;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @JsonView(Views.Limited.class)
    public int getNumPlayers() { return players == null ? 0 : players.size(); }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getJudgingPlayer() {
        return judgingPlayer;
    }

    public void setJudgingPlayer(Player judgingPlayer) {
        this.judgingPlayer = judgingPlayer;
    }

    public Set<Phrase> getPhraseSet() {
        return phraseSet;
    }

    public void setPhraseSet(Set<Phrase> phraseSet) {
        this.phraseSet = phraseSet;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        this.gameStateTime = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public ZonedDateTime getGameStateTime() {
        return gameStateTime;
    }

    public ZonedDateTime getGameTimeoutTime() {
        return gameTimeoutTime;
    }

    public void setGameTimeoutTime(ZonedDateTime gameTimeoutTime) {
        this.gameTimeoutTime = gameTimeoutTime;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public List<List<Phrase>> getPhraseSelections() {
        return phraseSelections;
    }

    @JsonView(Views.Full.class)
    public int getNumPlayersSelectedPhrases() {
        return phraseSelections == null ? 0 : phraseSelections.size();
    }

    public void setPhraseSelections(List<List<Phrase>> phraseSelections) {
        this.phraseSelections = phraseSelections;
    }

    public Player getLastWinningPlayer() {
        return lastWinningPlayer;
    }

    public void setLastWinningPlayer(Player lastWinningPlayer) {
        this.lastWinningPlayer = lastWinningPlayer;
    }

    public Integer getJudgeChoiceWinner() {
        return judgeChoiceWinner;
    }

    public void setJudgeChoiceWinner(Integer judgeChoiceWinner) {
        this.judgeChoiceWinner = judgeChoiceWinner;
    }

    public Boolean haveAllPlayersSelected() {
       return phraseSelections.size() == players.size() - 1; // minus 1 because judge doesn't choose.
    }

    public boolean isPhraseUpForVote(Integer voteIndex) {
        return voteIndex >= 0 && voteIndex < phraseSelections.size();
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }

    public boolean hasGameWinner() {
        return gameConfig.getMaxScore() == getTopScore();
    }

    public Player getRoundWinner() {
        if (judgeChoiceWinner == null || judgeChoiceWinner < 0 || judgeChoiceWinner >= phraseSelections.size()) {
            return null;
        }

        List<Phrase> winningPhrases = this.getPhraseSelections().get(judgeChoiceWinner);

        return players.stream()
                .filter(player -> player.getSelectedPhrases().equals(winningPhrases))
                .findFirst()
                .orElse(null);
    }

    public boolean isGameFull() {
        return players.size() >= gameConfig.getMaxPlayers();
    }

    public int getTopScore() {
        if (players == null) return 0;

        return players.stream()
                .map(Player::getScore).mapToInt(v -> v)
                .max()
                .orElse(0);
    }
}
