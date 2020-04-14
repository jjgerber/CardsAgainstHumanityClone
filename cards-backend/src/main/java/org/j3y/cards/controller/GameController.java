package org.j3y.cards.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.j3y.cards.exception.GameNotFoundException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.Views;
import org.j3y.cards.model.Game;
import org.j3y.cards.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/api/v1/games")
public class GameController extends BaseController {

    private final GameService gameService;

    @Autowired
    public GameController(final GameService gameService) {
        this.gameService = gameService;
    }

    @JsonView(Views.Full.class)
    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(@PathVariable String name) throws InterruptedException {
        Game game = gameService.getGameByName(name);
        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return game;
    }

    @JsonView(Views.Limited.class)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game createGame(@PathVariable String name, @RequestBody GameConfig gameConfig) throws InterruptedException {
        return gameService.createGame(name, getPlayer(), gameConfig);
    }

    @JsonView(Views.Full.class)
    @PutMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game updateGame(@PathVariable String name, @RequestBody GameConfig gameConfig) throws InterruptedException {
        Game game = gameService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return gameService.updateGame(game, getPlayer(), gameConfig);
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game startGame(@PathVariable String name) throws InterruptedException {
        Game game = gameService.getGameByName(name);

        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return gameService.startGame(game, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game joinGame(@PathVariable String name) throws InterruptedException {
        Game game = gameService.getGameByName(name);
        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return gameService.joinGame(game, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/leave", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game leaveGame(@PathVariable String name) throws InterruptedException {
        Game game = gameService.getGameByName(name);
        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return gameService.leaveGame(game, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/select-phrases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game selectPhrases(@PathVariable String name, @RequestBody List<String> selectedPhraseIds) throws InterruptedException {
        Game game = gameService.getGameByName(name);
        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return gameService.pickPhrasesForCard(game, selectedPhraseIds, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/select-winner/{winnerIndex}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game selectWinner(@PathVariable String name, @PathVariable Integer winnerIndex) throws InterruptedException {
        Game game = gameService.getGameByName(name);

        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }

        if (winnerIndex == null) {
            throw new InvalidActionException("You must supply a winner index.");
        }

        return gameService.vote(game, getPlayer(), winnerIndex);
    }
}
