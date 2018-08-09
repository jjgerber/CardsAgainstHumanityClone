package org.j3y.cards.controller;

import org.j3y.cards.model.gameplay.CardDeck;
import org.j3y.cards.model.gameplay.Game;
import org.j3y.cards.model.gameplay.Player;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.response.GameSummary;
import org.j3y.cards.service.DeckService;
import org.j3y.cards.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping
public class GameController {

    private GameService gameService;
    private DeckService deckService;

    @Autowired
    public GameController(GameService gameService,
                          DeckService deckService) {
        this.gameService = gameService;
        this.deckService = deckService;
    }

    @GetMapping(value = "/v1/game/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(@PathVariable String name) throws InterruptedException {
        return gameService.getGameByName(name);
    }

    @GetMapping(value = "/v1/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GameSummary> getAllGameNames() {
        return gameService.getAllGames();
    }

    @PostMapping(value = "/v1/game/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game createGame(@PathVariable String name,
                           @RequestBody GameConfig gameConfig,
                           HttpServletRequest request) {

        Player player = (Player) request.getSession().getAttribute("player");
        Set<CardDeck> deckSet = deckService.getDecksById(gameConfig.getDeckIds());
        return gameService.createGame(name, player, deckSet, gameConfig);
    }
}
