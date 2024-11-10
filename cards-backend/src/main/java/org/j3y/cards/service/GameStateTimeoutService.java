package org.j3y.cards.service;

import org.j3y.cards.model.Game;

public interface GameStateTimeoutService {
    void setChoosingTimeout(Game game);

    void setJudgingTimeout(Game game);

    void setDoneJudgingTimeout(Game game);

    void setGameOverTimeout(Game game);
}
