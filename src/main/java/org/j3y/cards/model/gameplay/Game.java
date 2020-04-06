package org.j3y.cards.model.gameplay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.j3y.cards.model.GameConfig;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Game {
    private String name;
    private Player owner;
    private Set<Card> cardSet;
    private Set<Phrase> phraseSet;
    private List<Player> players;
    private GameConfig gameConfig;

    // Stateful stuff
    @JsonIgnore private Semaphore mutex;
    private Player judgingPlayer;
    private Card currentCard;

    private GameState gameState;
    private LocalDateTime gameStateTime;

    @JsonIgnore private BidiMap<Player, Integer> playerPhraseSelectionIndexMap;
    private List<List<Phrase>> phraseSelections;

    private Integer judgeChoiceWinner;
    private Player lastWinningPlayer;

    public Game() {
        super();
        this.mutex = new Semaphore(1);
        this.playerPhraseSelectionIndexMap = new DualHashBidiMap<>();
        this.phraseSelections = new ArrayList<>();
        this.setGameState(GameState.LOBBY);
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
        this.gameStateTime = LocalDateTime.now();
    }

    public LocalDateTime getGameStateTime() {
        return gameStateTime;
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
        return players.stream()
                .map(Player::getScore).mapToInt(v -> v)
                .max()
                .orElse(0);
    }

}
