package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.Player;

import java.util.List;

public interface GameService {
    Game getGameByName(String name) throws InterruptedException;
    List<Game> getAllGames();

    void sendGameUpdate(Game game);
    void sendGameUpdate(Game game, Class view);

    Game createGame(String name, Player owner, GameConfig gameConfig) throws InterruptedException;
    Game startGame(Game game, Player requestingPlayer) throws InterruptedException;
    Game pickPhrasesForCard(Game game, List<String> selectedPhrasesIds, Player selectingPlayer) throws InterruptedException;
    Game vote(Game game, Player votingPlayer, Integer voteIndex) throws InterruptedException;
    Game joinGame(Game game, Player joiningPlayer) throws InterruptedException;
    Game leaveGame(Game game, Player leavingPlayer) throws InterruptedException;
    Game updateGame(Game game, Player updatingPlayer, GameConfig newGameConfig) throws InterruptedException;

    void setStateChoosing(Game game) throws InterruptedException;
    void setStateDoneChoosing(Game game) throws InterruptedException;
    void setStateDoneJudging(Game game) throws InterruptedException;
    void setStateGameOver(Game game) throws InterruptedException;
    void setStateLobby(Game game) throws InterruptedException;

}
