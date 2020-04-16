package org.j3y.cards.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.j3y.cards.model.Player;
import org.j3y.cards.model.Views;
import org.j3y.cards.service.GameWebsocketService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/api/v1/player")
public class PlayerController extends BaseController {

    private final GameWebsocketService gameWebsocketService;

    public PlayerController(GameWebsocketService gameWebsocketService) {
        this.gameWebsocketService = gameWebsocketService;
    }

    @JsonView(Views.LoggedInUser.class)
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player getPlayerInfo() {
        return getPlayer();
    }

    @JsonView(Views.LoggedInUser.class)
    @PostMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player setPlayerName(@PathVariable String name, @ApiIgnore HttpServletResponse response) {
        Player player = getPlayer();
        player.setPlayerName(name);

        Cookie nameCookie = new Cookie("playerName", name);
        nameCookie.setMaxAge(10 * 365 * 24 * 60 * 60);
        response.addCookie(nameCookie);

        if (player.getCurrentGame() != null) {
            gameWebsocketService.sendGameUpdate(player.getCurrentGame());
        }

        return player;
    }

}
