package org.j3y.cards.filter;

import org.j3y.cards.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Random;

@WebFilter({"/*"})
public class LoginFilter extends GenericFilterBean {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null || !(sc.getAuthentication() instanceof Player)) {
            Player player = new Player();

            // See if the user has a cookie set with username.
            Cookie nameCookie = WebUtils.getCookie((HttpServletRequest) request, "playerName");
            if (nameCookie != null) {
                logger.info("Setting name from cookie!");
                player.setPlayerName(nameCookie.getValue());
            } else {
                player.setPlayerName("Player " + new Random().nextInt(99999));
            }

            sc.setAuthentication(player);
        }

        chain.doFilter(request,response);
    }

}