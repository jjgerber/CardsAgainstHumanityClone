package org.j3y.cards.service;

import org.j3y.cards.model.Game;

public interface GameStateService {

    void setStateChoosing(Game game);
    void setStateDoneChoosing(Game game);
    void setStateJudging(Game game);
    void setStateDoneJudging(Game game);
    void setStateGameOver(Game game);
    void setStateLobby(Game game);

}
