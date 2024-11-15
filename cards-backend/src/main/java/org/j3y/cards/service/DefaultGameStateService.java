package org.j3y.cards.service;

import org.j3y.cards.model.Game;
import org.j3y.cards.model.GameState;
import org.j3y.cards.model.Phrase;
import org.j3y.cards.model.Player;
import org.j3y.cards.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DefaultGameStateService implements GameStateService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GameStateTimeoutService gameStateTimeoutService;
    private final GameWebsocketService gameWebsocketService;
    private final GameActionsService gameActionsService;

    @Autowired
    public DefaultGameStateService(
            final GameStateTimeoutService gameStateTimeoutService,
            final GameWebsocketService gameWebsocketService,
            @Lazy final GameActionsService gameActionsService
    ) {
        this.gameStateTimeoutService = gameStateTimeoutService;
        this.gameWebsocketService = gameWebsocketService;
        this.gameActionsService = gameActionsService;
    }

    @Override
    public void setStateChoosing(Game game) {
        // Kick all inactives
        kickInactives(game);
        if (game.getGameState() == GameState.ABANDONED) {
            // After kicking the game is now abandoned. Return without doing anything.
            return;
        }
        if (game.getPlayers().size() <= 2) {
            gameWebsocketService.sendGameChatMessage(game, "There are not enough players to play. Game set to Lobby.");
            setStateLobby(game);
            return;
        }

        try {
            game.getMutex().acquire();
            game.setGameState(GameState.CHOOSING);

            // Clear out the phrase selections
            game.getPlayersWhoHaveChosen().clear();
            game.getPhraseSelections().clear();
            game.setJudgeChoiceWinner(null);

            Player lastJudgingPlayer = game.getJudgingPlayer();
            Player nextJudgingPlayer;
            if (lastJudgingPlayer == null) {
                // No one is currently the judge. Pick a random person.
                nextJudgingPlayer = CollectionUtil.getRandomItemFromCollection(game.getPlayers());
            } else {
                int nextPlayerIdx = (game.getPlayers().indexOf(lastJudgingPlayer) + 1) % game.getPlayers().size();
                nextJudgingPlayer = game.getPlayers().get(nextPlayerIdx);
            }

            game.setJudgingPlayer(nextJudgingPlayer);
            game.setCurrentCard(CollectionUtil.getRandomItemFromCollection(game.getCardSet()));
            game.getCardSet().remove(game.getCurrentCard());

            // Give players more cards if they need them.
           gameActionsService.manageAllPlayersPhrases(game);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

        gameStateTimeoutService.setChoosingTimeout(game);
    }

    private void kickInactives(Game game) {
        List<Player> inactivePlayers = game.getPlayers().stream()
                .filter(player -> player.getMissedTurns() >= 3)
                .toList();

        for (Player inactivePlayer : inactivePlayers) {
            gameActionsService.leaveGame(game, inactivePlayer, false);
            gameWebsocketService.sendGameChatMessage(game, inactivePlayer.getPlayerName() + " missed 3 consecutive turns and has been removed from the game.");
        }
    }

    @Override
    public void setStateDoneChoosing(Game game) {
        try {
            game.getMutex().acquire();
            game.setGameState(GameState.DONE_CHOOSING);

            // For everyone who missed a turn, increment their missed turn counter
            game.getPlayers().stream()
                    .filter(player -> !game.getPlayersWhoHaveChosen().contains(player) && !player.equals(game.getJudgingPlayer()))
                    .forEach(Player::incrementMissedTurns);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

        setStateJudging(game);
    }

    @Override
    public void setStateJudging(Game game) {

        boolean hasNoSelections = false;
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.JUDGING);

            if (game.getPhraseSelections().isEmpty()) {
                hasNoSelections = true;
            } else {
                // Shuffle the selections.
                Collections.shuffle(game.getPhraseSelections());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

        if (hasNoSelections) {
            setStateDoneJudging(game);
        } else {
            // Game update will be sent to clients by the gameStateTimeoutService w/ timeout time.
            gameStateTimeoutService.setJudgingTimeout(game);
        }
    }

    @Override
    public void setStateDoneJudging(Game game) {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.DONE_JUDGING);

            boolean returnPhrases = false;

            Player winner = game.getRoundWinner();
            if (winner == null) {
                returnPhrases = true;

                if (!game.getPlayersWhoHaveChosen().isEmpty() && game.getJudgingPlayer() != null) {
                    game.getJudgingPlayer().incrementMissedTurns(); // Judge missed a turn, increment their counter.
                }
            } else {
                winner.incrementScore();
                game.setLastWinningPlayer(winner);
                gameWebsocketService.sendGameChatMessage(game, winner.getPlayerName() + " has won the round.");
            }

            game.getPlayersWhoHaveChosen().clear();

            for (Player player : game.getPlayers()) {
                try {
                    player.getMutex().acquire();

                    List<Phrase> selectedPhrases = player.getSelectedPhrases();
                    if (selectedPhrases.isEmpty()) continue;

                    if (returnPhrases) {
                        player.getPhrases().addAll(player.getSelectedPhrases());
                    }

                    selectedPhrases.clear();
                    gameWebsocketService.sendPlayerUpdate(player);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while trying to give player back their cards.");
                } finally {
                    player.getMutex().release();
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

        // Game update will be sent to clients by the gameStateTimeoutService w/ timeout time.
        gameStateTimeoutService.setDoneJudgingTimeout(game);
    }

    @Override
    public void setStateGameOver(Game game) {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.GAME_OVER);
            game.setJudgingPlayer(null);
            game.setLastWinningPlayer(null);
            game.setJudgeChoiceWinner(null);
            game.setCurrentCard(null);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

        // Game update will be sent to clients by the gameStateTimeoutService w/ timeout time.
        gameStateTimeoutService.setGameOverTimeout(game);
    }

    @Override
    public void setStateLobby(Game game) {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.LOBBY);
            game.setCardSet(null);
            game.setPhraseSet(null);
            game.setCurrentCard(null);
            game.setJudgingPlayer(null);
            game.setJudgeChoiceWinner(null);
            game.setLastWinningPlayer(null);
            game.getPlayers().forEach(player -> {
                player.reset();
                gameWebsocketService.sendPlayerUpdate(player);
            });

            gameWebsocketService.sendGameUpdate(game);
            gameWebsocketService.sendLobbiesUpdate();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            game.getMutex().release();
        }

    }

}
