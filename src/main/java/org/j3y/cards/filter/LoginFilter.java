package org.j3y.cards.filter;

import org.j3y.cards.model.gameplay.Player;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@WebFilter({"/*"})
public class LoginFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession();
        Object playerObj = session.getAttribute("player");

        if (playerObj == null) {
            Player player = new Player();
            player.setName("Player " + new Random().nextInt(99999));
            player.setPlayerId(UUID.randomUUID().toString());
            session.setAttribute("player", player);
        }

        chain.doFilter(request,response);
    }

}
