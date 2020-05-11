package org.j3y.cards.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.j3y.cards.exception.GameNotFoundException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.GameState;
import org.j3y.cards.model.Views;
import org.j3y.cards.service.GameActionsService;
import org.j3y.cards.service.GameManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping(path = "/api/v1/games")
public class GameController extends BaseController {

    private static final String GAME_NAME_REGEX = "^[a-zA-Z0-9 ]{1,20}$";

    private final GameManagementService gameManagementService;
    private final GameActionsService gameActionsService;
    private final ObjectMapper mapper;

    @Autowired
    public GameController(
            final GameManagementService gameManagementService,
            final GameActionsService gameActionsService,
            final ObjectMapper mapper
    ) {
        this.gameManagementService = gameManagementService;
        this.gameActionsService = gameActionsService;
        this.mapper = mapper;
    }

    @JsonView(Views.Full.class)
    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getGame(@PathVariable String name) throws InterruptedException, JsonProcessingException {
        Game game = gameManagementService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return getGameJson(game);
    }

    @JsonView(Views.Limited.class)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Game> getAllGames() {
        return gameManagementService.getAllGames();
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game createGame(@PathVariable String name, @RequestBody @Valid GameConfig gameConfig) throws InterruptedException {
        if (name == null || !name.matches(GAME_NAME_REGEX)) {
            throw new InvalidActionException("Game name was invalid.");
        }
        return gameActionsService.createGame(name, getPlayer(), gameConfig);
    }

    @JsonView(Views.Full.class)
    @PutMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game updateGame(@PathVariable String name, @RequestBody @Valid GameConfig gameConfig) throws InterruptedException {
        Game game = gameManagementService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return gameActionsService.updateGame(game, getPlayer(), gameConfig);
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game startGame(@PathVariable String name) throws InterruptedException {
        Game game = gameManagementService.getGameByName(name);

        if (game == null) {
            throw new GameNotFoundException();
        }
        return gameActionsService.startGame(game, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public String joinGame(@PathVariable String name) throws InterruptedException, JsonProcessingException {
        Game game = gameManagementService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return getGameJson(gameActionsService.joinGame(game, getPlayer()));
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/leave", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game leaveGame(@PathVariable String name) throws InterruptedException {
        Game game = gameManagementService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return gameActionsService.leaveGame(game, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/select-phrases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game selectPhrases(@PathVariable String name, @RequestBody List<String> selectedPhraseIds) throws InterruptedException {
        Game game = gameManagementService.getGameByName(name);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return gameActionsService.pickPhrasesForCard(game, selectedPhraseIds, getPlayer());
    }

    @JsonView(Views.Full.class)
    @PostMapping(value = "/{name}/select-winner/{winnerIndex}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game selectWinner(@PathVariable String name, @PathVariable Integer winnerIndex) throws InterruptedException {
        Game game = gameManagementService.getGameByName(name);

        if (game == null) {
            throw new GameNotFoundException();
        }

        if (winnerIndex == null) {
            throw new InvalidActionException("You must supply a winner index.");
        }

        return gameActionsService.vote(game, getPlayer(), winnerIndex);
    }

    /**
     * This method is used when there may be a need to retrieve more sensitive data in the request,
     * e.g. joining a game that is currently in Judging state - we want to send them all the phrases
     * and to do that will need to use the Judging view.
     *
     * @param game Game to convert to JSON
     * @return Game converted to JSON with the appropriate view for it's current state.
     * @throws JsonProcessingException
     */
    private String getGameJson(Game game) throws JsonProcessingException {
        Class view = Views.Full.class;
        GameState currentState = game.getGameState();
        if (currentState == GameState.JUDGING || currentState == GameState.DONE_JUDGING) {
            view = Views.Judging.class;
        }
        return mapper.writerWithView(view).writeValueAsString(game);
    }
}
