package org.j3y.cards.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.j3y.cards.model.Views;
import org.j3y.cards.model.Player;
import org.j3y.cards.service.GameService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/player")
public class PlayerController extends BaseController {

    private final GameService gameService;

    public PlayerController(GameService gameService) {
        this.gameService = gameService;
    }

    @JsonView(Views.LoggedInUser.class)
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player getPlayerInfo() {
        return getPlayer();
    }

    @JsonView(Views.LoggedInUser.class)
    @PostMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player setPlayerName(@PathVariable  String name) {
        Player player = getPlayer();
        player.setPlayerName(name);

        if (player.getCurrentGame() != null) {
            gameService.sendGameUpdate(player.getCurrentGame());
        }

        return player;
    }

}
