package org.j3y.cards.controller;

import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.gameplay.Game;
import org.j3y.cards.model.gameplay.Player;
import org.j3y.cards.response.GameSummary;
import org.j3y.cards.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpSession;
import java.util.List;


@RestController
@RequestMapping(path = "/v1/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(final GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(@PathVariable String name) throws InterruptedException {
        return gameService.getGameByName(name);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GameSummary> getAllGameNames() {
        return gameService.getAllGames();
    }

    @PostMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game createGame(@PathVariable String name, @RequestBody GameConfig gameConfig, @ApiIgnore HttpSession session) throws InterruptedException {
        Player player = (Player) session.getAttribute("player");
        return gameService.createGame(name, player, gameConfig);
    }

    @PostMapping(value = "/{name}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game startGame(@PathVariable String name, @ApiIgnore HttpSession session) throws InterruptedException {
        Player player = (Player) session.getAttribute("player");
        Game game = gameService.getGameByName(name);

        if (game == null) {
            throw new InvalidActionException("That game does not exist.");
        }
        return gameService.startGame(game, player);
    }
}
