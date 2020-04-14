package org.j3y.cards.filter;

import org.j3y.cards.model.Player;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.Random;

@WebFilter({"/*"})
public class LoginFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        /*
        Player player = new Player();
        String playerName;

        Cookie playerNameCookie = WebUtils.getCookie((HttpServletRequest) request, "playerName");
        if (playerNameCookie == null) {
            playerName = "Player " + new Random().nextInt(99999);
        } else {
            playerName = playerNameCookie.getValue();
        }
        */

        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null || !(sc.getAuthentication() instanceof Player)) {
            Player player = new Player();
            player.setPlayerName("Player " + new Random().nextInt(99999));
            sc.setAuthentication(player);
        }

        chain.doFilter(request,response);
    }

}