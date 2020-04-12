package org.j3y.cards.model.gameplay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.Views;

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

    // Stateful stuff
    @JsonIgnore private Semaphore mutex;
    @JsonView(Views.Full.class) private Player judgingPlayer;
    @JsonView(Views.Full.class) private Card currentCard;

    @JsonView(Views.Limited.class) private GameState gameState;
    @JsonView(Views.Limited.class) private ZonedDateTime gameStateTime;
    @JsonView(Views.Limited.class) private ZonedDateTime gameTimeoutTime;

    @JsonIgnore private BidiMap<Player, Integer> playerPhraseSelectionIndexMap;
    @JsonView(Views.Full.class) private List<List<Phrase>> phraseSelections;

    @JsonView(Views.Full.class) private Integer judgeChoiceWinner;
    @JsonView(Views.Full.class) private Player lastWinningPlayer;

    public Game() {
        this.uuid = UUID.randomUUID().toString();
        this.mutex = new Semaphore(1);
        this.playerPhraseSelectionIndexMap = new DualHashBidiMap<>();
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
        this.gameStateTime = ZonedDateTime.now();
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

    public Map<Player, Integer> getPlayerPhraseSelectionIndexMap() {
        return playerPhraseSelectionIndexMap;
    }

    public void resetPlayerPhraseSelections() {
        this.playerPhraseSelectionIndexMap.clear();
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

    public Boolean hasPlayerSelected(Player player) {
        return playerPhraseSelectionIndexMap.containsKey(player);
    }

    public Boolean haveAllPlayersSelected() {
       return players.size() == phraseSelections.size();
    }

    public boolean isPhraseUpForVote(Integer voteIndex) {
        return phraseSelections.get(voteIndex) != null;
    }

    public boolean hasPhrases(Collection<Phrase> phrases) {
        return phraseSet.containsAll(phrases);
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }

    public boolean hasGameWinner() {
        return gameConfig.getMaxScore() == getTopScore();
    }

    public Player getRoundWinner() {
        if (judgeChoiceWinner == null) {
            return null;
        }

        return playerPhraseSelectionIndexMap.inverseBidiMap().get(judgeChoiceWinner);
    }

    public Player getGameWinner() {
        int topScore = getTopScore();

        if (topScore != gameConfig.getMaxScore()) {
            return null;
        }

        return players.stream()
                .filter(player -> player.getScore() == topScore)
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
