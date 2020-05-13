package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class DefaultGameStateTimeoutService implements GameStateTimeoutService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GameStateService gameStateService;
    private final GameWebsocketService gameWebsocketService;
    private final int winnerTimeout;
    private final int gameOverTimeout;

    @Autowired
    public DefaultGameStateTimeoutService(
            @Lazy final GameStateService gameStateService,
            final GameWebsocketService gameWebsocketService,
            @Value("${game.winner-timeout}") final int winnerTimeout,
            @Value("${game.game-over-timeout}") final int gameOverTimeout
    ) {
        this.gameStateService = gameStateService;
        this.gameWebsocketService = gameWebsocketService;
        this.winnerTimeout = winnerTimeout;
        this.gameOverTimeout = gameOverTimeout;
    }

    @Override
    @Async
    public void setChoosingTimeout(Game game) throws InterruptedException {
        int turnTimeoutSeconds = game.getGameConfig().getTurnTimeout();
        int turnTimeoutMs = turnTimeoutSeconds * 1000;
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(turnTimeoutSeconds);
        logger.info("Now Choosing - Timer set for {} seconds for state timeout.", turnTimeoutSeconds);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);
        Thread.sleep(turnTimeoutMs);

        if (game.getGameState() == GameState.CHOOSING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
            logger.info("Game State Timed Out - state 'CHOOSING' took over {} seconds.", turnTimeoutSeconds);
            gameStateService.setStateDoneChoosing(game);
        }
    }

    @Override
    @Async
    public void setJudgingTimeout(Game game) throws InterruptedException {
        int turnTimeoutSeconds = game.getGameConfig().getTurnTimeout();
        int turnTimeoutMs = turnTimeoutSeconds * 1000;
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(turnTimeoutSeconds);
        logger.info("Now Judging - Timer set for {} seconds for state timeout.", turnTimeoutSeconds);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);
        Thread.sleep(turnTimeoutMs);

        if (game.getGameState() == GameState.JUDGING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
            logger.info("Game State Timed Out - state 'JUDGING' took over {} seconds.", turnTimeoutSeconds);
            gameStateService.setStateDoneJudging(game);
        }
    }

    @Override
    @Async
    public void setDoneJudgingTimeout(Game game) throws InterruptedException {
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(winnerTimeout);
        logger.info("Now Done Judging - Timer set for {} seconds for state timeout.", winnerTimeout);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);
        Thread.sleep(winnerTimeout * 1000);

        if (game.getGameState() == GameState.DONE_JUDGING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
            logger.info("Game State Timed Out - state 'DONE_JUDGING' took over {} seconds.", winnerTimeout);
            if (game.hasGameWinner()) {
                gameStateService.setStateGameOver(game);
            } else {
                gameStateService.setStateChoosing(game);
            }
        }
    }

    @Override
    @Async
    public void setGameOverTimeout(Game game) throws InterruptedException {
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(gameOverTimeout);
        logger.info("Now Game Over - Timer set for {} seconds for state timeout.", winnerTimeout);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);
        Thread.sleep(gameOverTimeout * 1000);

        if (game.getGameState() == GameState.GAME_OVER && timeoutTime.isEqual(game.getGameTimeoutTime())) {
            gameStateService.setStateLobby(game);
        }
    }

}
