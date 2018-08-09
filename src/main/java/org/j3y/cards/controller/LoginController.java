package org.j3y.cards.controller;

import org.j3y.cards.model.gameplay.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class LoginController {

    @Value("${auth.redirect-uri}")
    private String redirectUrl;

    @GetMapping("/login")
    public void login(@RequestParam String userName,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        Player player = new Player();
        player.setName(userName);
        player.setPhrases(null);
        player.setCurrentGame(null);
        request.getSession().setAttribute("player", player);

        response.sendRedirect(redirectUrl);
    }
}
