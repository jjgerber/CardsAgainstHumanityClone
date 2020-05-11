package org.j3y.cards.service;

import org.j3y.cards.model.Game;

public interface GameStateService {

    void setStateChoosing(Game game) throws InterruptedException;
    void setStateDoneChoosing(Game game) throws InterruptedException;
    void setStateJudging(Game game) throws InterruptedException;
    void setStateDoneJudging(Game game) throws InterruptedException;
    void setStateGameOver(Game game) throws InterruptedException;
    void setStateLobby(Game game) throws InterruptedException;

}
