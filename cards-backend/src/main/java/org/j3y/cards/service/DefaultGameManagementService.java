package org.j3y.cards.service;

import org.j3y.cards.exception.GameNotFoundException;
import org.j3y.cards.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultGameManagementService implements GameManagementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Game> gameMap;

    public DefaultGameManagementService() {
        this.gameMap = new HashMap<>();
    }

    @Override
    public Game getGameByName(String name) {
        return gameMap.get(name);
    }

    @Override
    public Collection<Game> getAllGames() {
        return gameMap.values();
    }

    @Override
    public Game addGame(Game game) {
        return gameMap.put(game.getName(), game);
    }

    @Override
    public Game removeGame(String gameName) {
        if (!gameMap.containsKey(gameName)) {
            throw new GameNotFoundException("That game does not exist.");
        }
        return gameMap.remove(gameName);
    }

    @Scheduled(fixedDelay = 60000)
    @Override
    public void purgeInactiveGames() {
        // TODO: Make this a configuration property
        ZonedDateTime oldLobbyCuttoffTime = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(30);
        List<Game> games = gameMap.values()
                .stream()
                .filter(game -> game.getGameStateTime().isBefore(oldLobbyCuttoffTime))
                .collect(Collectors.toList());
        if (!games.isEmpty()) {
            logger.info("Purging these games from memory for lack of activity: {}", games);
            games.stream().map(Game::getPlayers).forEach(players -> players.forEach(player -> player.setCurrentGame(null)));
            games.stream().map(Game::getName).forEach(gameMap::remove);
        }
    }
}
