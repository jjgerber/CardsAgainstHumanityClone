package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameState;
import org.j3y.cards.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class DefaultGameStateTimeoutService implements GameStateTimeoutService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GameStateService gameStateService;
    private final GameWebsocketService gameWebsocketService;
    private final TaskScheduler taskScheduler;
    private final int winnerTimeout;
    private final int gameOverTimeout;

    @Autowired
    public DefaultGameStateTimeoutService(
            @Lazy final GameStateService gameStateService,
            final GameWebsocketService gameWebsocketService,
            final TaskScheduler taskScheduler,
            @Value("${game.winner-timeout}") final int winnerTimeout,
            @Value("${game.game-over-timeout}") final int gameOverTimeout
    ) {
        this.gameStateService = gameStateService;
        this.gameWebsocketService = gameWebsocketService;
        this.taskScheduler = taskScheduler;
        this.winnerTimeout = winnerTimeout;
        this.gameOverTimeout = gameOverTimeout;
    }

    @Override
    public void setChoosingTimeout(Game game) {
        int turnTimeoutSeconds = game.getGameConfig().getTurnTimeout();
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(turnTimeoutSeconds);
        logger.info("Now Choosing - Timer set for {} seconds for state timeout.", turnTimeoutSeconds);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);

        Runnable task = () -> {
            if (game.getGameState() == GameState.CHOOSING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
                logger.info("Game State Timed Out - state 'CHOOSING' took over {} seconds.", turnTimeoutSeconds);
                gameStateService.setStateDoneChoosing(game);
            }
        };

        taskScheduler.schedule(task, timeoutTime.toInstant());
    }

    @Override
    public void setJudgingTimeout(Game game) {
        int turnTimeoutSeconds = game.getGameConfig().getTurnTimeout();
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(turnTimeoutSeconds);
        logger.info("Now Judging - Timer set for {} seconds for state timeout.", turnTimeoutSeconds);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);

        Runnable task = () -> {
            if (game.getGameState() == GameState.JUDGING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
                logger.info("Game State Timed Out - state 'JUDGING' took over {} seconds.", turnTimeoutSeconds);
                gameStateService.setStateDoneJudging(game);
            }
        };

        taskScheduler.schedule(task, timeoutTime.toInstant());
    }

    @Override
    public void setDoneJudgingTimeout(Game game) {
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(winnerTimeout);
        logger.info("Now Done Judging - Timer set for {} seconds for state timeout.", winnerTimeout);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);

        Runnable task = () -> {
            if (game.getGameState() == GameState.DONE_JUDGING && timeoutTime.isEqual(game.getGameTimeoutTime())) {
                logger.info("Game State Timed Out - state 'DONE_JUDGING' took over {} seconds.", winnerTimeout);
                Player gameWinner = game.getGameWinner();
                if (gameWinner != null) {
                    gameWebsocketService.sendGameChatMessage(game, gameWinner.getPlayerName() + " has won the game!");
                    gameStateService.setStateGameOver(game);
                } else {
                    gameStateService.setStateChoosing(game);
                }
            }
        };

        taskScheduler.schedule(task, timeoutTime.toInstant());
    }

    @Override
    public void setGameOverTimeout(Game game) {
        ZonedDateTime timeoutTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(gameOverTimeout);
        logger.info("Now Game Over - Timer set for {} seconds for state timeout.", winnerTimeout);
        game.setGameTimeoutTime(timeoutTime);
        gameWebsocketService.sendGameUpdate(game);


        Runnable task = () -> {
            if (game.getGameState() == GameState.GAME_OVER && timeoutTime.isEqual(game.getGameTimeoutTime())) {
                gameStateService.setStateLobby(game);
            }
        };

        taskScheduler.schedule(task, timeoutTime.toInstant());
    }

}
