package org.j3y.cards.service;

import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.gameplay.*;
import org.j3y.cards.response.GameSummary;

import java.util.List;
import java.util.Set;

public interface GameService {
    Game getGameByName(String name) throws InterruptedException;
    List<GameSummary> getAllGames();

    Game createGame(String name, Player owner, Set<CardDeck> deckSet, GameConfig gameConfig);
    Game startGame(Game game, Player requestingPlayer) throws InterruptedException;
    Game pickPhrasesForCard(Game game, List<Phrase> selectedPhrases, Player selectingPlayer) throws InterruptedException;
    Game vote(Game game, Player votingPlayer, Integer voteIndex) throws InterruptedException;
    Game joinGame(Game game, Player joiningPlayer) throws InterruptedException;
}
