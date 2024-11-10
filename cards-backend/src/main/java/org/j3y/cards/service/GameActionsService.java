package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.Player;

import java.util.List;

public interface GameActionsService {
    Game createGame(String name, Player owner, GameConfig gameConfig);
    Game startGame(Game game, Player requestingPlayer);
    Game pickPhrasesForCard(Game game, List<String> selectedPhrasesIds, Player selectingPlayer);
    Game vote(Game game, Player votingPlayer, Integer voteIndex);
    Game joinGame(Game game, Player joiningPlayer);
    Game leaveGame(Game game, Player leavingPlayer, boolean sendGameUpdate);
    Game updateGame(Game game, Player updatingPlayer, GameConfig newGameConfig);
    void manageAllPlayersPhrases(Game game);
}
