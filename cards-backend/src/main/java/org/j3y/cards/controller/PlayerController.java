package org.j3y.cards.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.model.Game;
import org.j3y.cards.model.Player;
import org.j3y.cards.model.Views;
import org.j3y.cards.service.GameWebsocketService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/api/v1/player")
public class PlayerController extends BaseController {

    private final GameWebsocketService gameWebsocketService;

    private static final String USERNAME_FORMAT_REGEX = "^[a-zA-Z0-9 \\-'!?.,]{1,20}$";

    public PlayerController(GameWebsocketService gameWebsocketService) {
        this.gameWebsocketService = gameWebsocketService;
    }

    @JsonView(Views.LoggedInUser.class)
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player getPlayerInfo() {
        return getPlayer();
    }

    @JsonView(Views.LoggedInUser.class)
    @PostMapping(value = "/name", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player setPlayerName(@RequestBody String name, HttpServletResponse response) {
        if (!name.matches(USERNAME_FORMAT_REGEX)) {
            throw new InvalidActionException("Your name contained invalid characters or was too long.");
        }

        Player player = getPlayer();
        String previousName = player.getPlayerName();
        player.setPlayerName(name);

        Cookie nameCookie = new Cookie("playerName", UriUtils.encode(name, StandardCharsets.UTF_8));
        nameCookie.setMaxAge(365 * 24 * 60 * 60); // set name cookie for a year.
        nameCookie.setPath("/");
        nameCookie.setHttpOnly(true);
        response.addCookie(nameCookie);


        if (player.getCurrentGame() != null) {
            Game game = player.getCurrentGame();
            gameWebsocketService.sendGameUpdate(game);
            gameWebsocketService.sendGameChatMessage(game, previousName + " changed their name to " + name);
        }

        return player;
    }

}
