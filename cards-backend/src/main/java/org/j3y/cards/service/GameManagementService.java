package org.j3y.cards.service;

import org.j3y.cards.model.Game;

import java.util.Collection;

public interface GameManagementService {
    Game getGameByName(String name) throws InterruptedException;
    Collection<Game> getAllGames();
    Game addGame(Game game);
    Game removeGame(String gameName);

    String getGameJson(Game game);

    void purgeInactiveGames();
}
