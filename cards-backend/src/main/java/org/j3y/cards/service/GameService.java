package org.j3y.cards.service;

import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.gameplay.Game;
import org.j3y.cards.model.gameplay.Phrase;
import org.j3y.cards.model.gameplay.Player;
import org.j3y.cards.response.GameSummary;

import java.util.List;

public interface GameService {
    Game getGameByName(String name) throws InterruptedException;
    List<GameSummary> getAllGames();

    Game createGame(String name, Player owner, GameConfig gameConfig) throws InterruptedException;
    Game startGame(Game game, Player requestingPlayer) throws InterruptedException;
    Game pickPhrasesForCard(Game game, List<Phrase> selectedPhrases, Player selectingPlayer) throws InterruptedException;
    Game vote(Game game, Player votingPlayer, Integer voteIndex) throws InterruptedException;
    Game joinGame(Game game, Player joiningPlayer) throws InterruptedException;
    Game leaveGame(Game game, Player leavingPlayer) throws InterruptedException;

    void setStateChoosing(Game game) throws InterruptedException;
    void setStateDoneChoosing(Game game) throws InterruptedException;
    void setStateDoneJudging(Game game) throws InterruptedException;
    void setStateGameOver(Game game) throws InterruptedException;
    void setStateLobby(Game game) throws InterruptedException;

}
