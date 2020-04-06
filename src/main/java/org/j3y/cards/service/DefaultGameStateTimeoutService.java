package org.j3y.cards.service;

import org.j3y.cards.model.gameplay.Game;
import org.j3y.cards.model.gameplay.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DefaultGameStateTimeoutService implements GameStateTimeoutService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GameService gameService;
    private final int winnerTimeout;
    private final int gameOverTimeout;

    @Autowired
    public DefaultGameStateTimeoutService(
            @Lazy final GameService gameService,
            @Value("${game.winner-timeout}") final int winnerTimeout,
            @Value("${game.game-over-timeout}") final int gameOverTimeout
    ) {
        this.gameService = gameService;
        this.winnerTimeout = winnerTimeout;
        this.gameOverTimeout = gameOverTimeout;
    }

    @Override
    @Async
    public void setChoosingTimeout(Game game) throws InterruptedException {
        int turnTimeout = game.getGameConfig().getTurnTimeout();
        logger.info("Now Choosing - Timer set for {} millseconds for state timeout.", turnTimeout);
        Thread.sleep(turnTimeout);

        if (game.getGameState() == GameState.CHOOSING) {
            logger.info("Game State Timed Out - state 'CHOOSING' took over {} ms.", turnTimeout);
            gameService.setStateDoneChoosing(game);
        }
    }

    @Override
    public void setJudgingTimeout(Game game) throws InterruptedException {
        int turnTimeout = game.getGameConfig().getTurnTimeout();
        logger.info("Now Judging - Timer set for {} millseconds for state timeout.", turnTimeout);
        Thread.sleep(turnTimeout);

        if (game.getGameState() == GameState.JUDGING) {
            logger.info("Game State Timed Out - state 'JUDGING' took over {} ms.", turnTimeout);
            gameService.setStateDoneJudging(game);
        }
    }

    @Override
    public void setDoneJudgingTimeout(Game game) throws InterruptedException {
        logger.info("Now Done Judging - Timer set for {} millseconds for state timeout.", winnerTimeout);
        Thread.sleep(winnerTimeout);

        if (game.getGameState() == GameState.DONE_JUDGING) {
            logger.info("Game State Timed Out - state 'DONE_JUDGING' took over {} ms.", winnerTimeout);
            if (game.hasGameWinner()) {
                gameService.setStateGameOver(game);
            } else {
                gameService.setStateChoosing(game);
            }
        }
    }

    @Override
    public void setGameOverTimeout(Game game) throws InterruptedException {
        Thread.sleep(gameOverTimeout);

        if (game.getGameState() == GameState.GAME_OVER) {
            gameService.setStateLobby(game);
        }
    }
}
