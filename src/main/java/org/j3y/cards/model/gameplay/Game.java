package org.j3y.cards.model.gameplay;

import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.exception.WrongGameStateException;
import org.j3y.cards.model.GameConfig;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Game {
    private String name;
    private Set<Card> cardSet;
    private Set<Phrase> phraseSet;
    private List<Player> playerList;
    private GameConfig gameConfig;

    // Stateful stuff
    private Semaphore mutex;
    private Player judgingPlayer;
    private Card currentCard;

    private GameState gameState;
    private Long gameStateUnixTime;

    private Map<Player, Integer> playerPhraseSelectionIndexMap;
    private List<List<Phrase>> phraseSelections;
    private Map<Player, Integer> scores;
    private Map<Player, Integer> votes;

    public Game() {
        super();
        this.mutex = new Semaphore(1);
        this.playerPhraseSelectionIndexMap = new HashMap<>();
        this.phraseSelections = new ArrayList<>();
        this.scores = new HashMap<>();
        this.votes = new HashMap<>();
        this.setGameState(GameState.LOBBY);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
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
        this.gameStateUnixTime = System.currentTimeMillis();
    }

    public Long getGameStateUnixTime() {
        return gameStateUnixTime;
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

    public Map<Player, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<Player, Integer> scores) {
        this.scores = scores;
    }

    public Map<Player, Integer> getVotes() {
        return votes;
    }

    public void setVotes(Map<Player, Integer> votes) {
        this.votes = votes;
    }

    public Map<Player, Integer> getPlayerPhraseSelectionIndexMap() {
        return playerPhraseSelectionIndexMap;
    }

    public void setPlayerPhraseSelectionIndexMap(Map<Player, Integer> playerPhraseSelectionIndexMap) {
        this.playerPhraseSelectionIndexMap = playerPhraseSelectionIndexMap;
    }

    public List<List<Phrase>> getPhraseSelections() {
        return phraseSelections;
    }

    public void setPhraseSelections(List<List<Phrase>> phraseSelections) {
        this.phraseSelections = phraseSelections;
    }

    public Boolean hasPlayerSelected(Player player) {
        return playerPhraseSelectionIndexMap.keySet().contains(player);
    }

    public Boolean haveAllPlayersSelected() {
       return playerList.size() == phraseSelections.size();
    }

    public Boolean haveAllPlayersVoted() {
        return playerList.size() == votes.size();
    }

    public Boolean hasPlayerVoted(Player player) {
        return votes.keySet().contains(player);
    }

    public Boolean isPhraseUpForVote(Integer voteIndex) {
        return phraseSelections.get(voteIndex) != null;
    }

    public Boolean hasPhrases(Collection<Phrase> phrases) {
        return phraseSet.containsAll(phrases);
    }

    public Boolean hasPlayer(Player player) {
        return playerList.contains(player);
    }

    public Boolean isGameFull() {
        return playerList.size() >= gameConfig.getMaxPlayers();
    }

    public List<Player> getTopVotesPlayers() {
        List<Player> topVotedPlayers = new ArrayList<>();
        Integer topNumVotes = 0;
        Map.Entry<Player, Integer> topPlayerScore = null;
        for (Map.Entry<Player, Integer> playerVotes : votes.entrySet()) {
            Integer numVotes = playerVotes.getValue();
            if (numVotes.compareTo(topNumVotes) == 0) {
                topVotedPlayers.add(playerVotes.getKey());
            } else if (numVotes.compareTo(topNumVotes) > 0) {
                topNumVotes = numVotes;
                topVotedPlayers.clear();
                topVotedPlayers.add(playerVotes.getKey());
            }
        }

        return topVotedPlayers;
    }

    public Map.Entry<Player, Integer> getTopScore() {
        Integer maxScore = 0;
        Map.Entry<Player, Integer> topPlayerScore = null;
        for (Map.Entry<Player, Integer> playerScore : scores.entrySet()) {
            if (playerScore.getValue() > maxScore) {
                maxScore = playerScore.getValue();
                topPlayerScore = playerScore;
            }
        }

        return topPlayerScore;
    }

    public void increasePlayerScore(Player player) {
        Integer score = scores.get(player);

        if (score == null) {
            score = 1;
        } else {
            score = score + 1;
        }

        scores.put(player, score);

        // Check if max score was reached.
        if (score >= gameConfig.getMaxScore()) {
            setGameState(GameState.WINNER);
        }
    }

}
