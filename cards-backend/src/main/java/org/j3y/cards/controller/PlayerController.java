package org.j3y.cards.controller;

import org.j3y.cards.model.gameplay.Player;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/v1/player")
public class PlayerController {

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player getPlayerInfo(@ApiIgnore HttpSession session) {
        return (Player) session.getAttribute("player");
    }

    @PostMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player setPlayerName(@PathVariable  String name, @ApiIgnore HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        player.setName(name);

        return player;
    }

}
