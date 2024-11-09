package org.j3y.cards.filter;

import jakarta.servlet.http.HttpServletResponse;
import org.j3y.cards.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@WebFilter({"/*"})
public class LoginFilter extends GenericFilterBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null || !(sc.getAuthentication() instanceof Player)) {
            Player player = new Player();

            // See if the user has a cookie set with username.
            Cookie nameCookie = WebUtils.getCookie((HttpServletRequest) request, "playerName");
            if (nameCookie != null) {
                player.setPlayerName(UriUtils.decode(nameCookie.getValue(), StandardCharsets.UTF_8));
            } else {
                player.setPlayerName("Player " + new Random().nextInt(99999));
            }

            sc.setAuthentication(player);
            securityContextRepository.saveContext(sc, (HttpServletRequest) request, (HttpServletResponse) response);
        }

        chain.doFilter(request, response);
    }

}