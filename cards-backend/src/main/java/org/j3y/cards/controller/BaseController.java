package org.j3y.cards.controller;

import org.j3y.cards.model.gameplay.Player;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {
    public Player getPlayer() {
        SecurityContext sc = SecurityContextHolder.getContext();
        return (Player) sc.getAuthentication();
    }
}
