package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.Player;

public interface GameWebsocketService {

    void sendPlayerUpdate(Player player);
    void sendGameUpdate(Game game);
    void sendGameChatMessage(Game game, String message);
    void sendLobbiesUpdate();

}
