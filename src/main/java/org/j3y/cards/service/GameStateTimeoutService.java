package org.j3y.cards.service;

import org.j3y.cards.model.gameplay.Game;
import org.springframework.scheduling.annotation.Async;

public interface GameStateTimeoutService {
    @Async
    void setChoosingTimeout(Game game) throws InterruptedException;

    @Async
    void setJudgingTimeout(Game game) throws InterruptedException;

    @Async
    void setDoneJudgingTimeout(Game game) throws InterruptedException;

    @Async
    void setGameOverTimeout(Game game) throws InterruptedException;
}
