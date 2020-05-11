package org.j3y.cards.service;

import org.j3y.cards.exception.GameAlreadyExistsException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.exception.WrongGameStateException;
import org.j3y.cards.model.*;
import org.j3y.cards.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultGameActionsService implements GameActionsService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GameManagementService gameManagementService;
    private final GameStateService gameStateService;
    private final DeckService deckService;
    private final GameWebsocketService gameWebsocketService;

    @Autowired
    public DefaultGameActionsService(
            final GameManagementService gameManagementService,
            final GameStateService gameStateService,
            final DeckService deckService,
            final GameWebsocketService gameWebsocketService
    ) {
        this.gameManagementService = gameManagementService;
        this.gameStateService = gameStateService;
        this.deckService = deckService;
        this.gameWebsocketService = gameWebsocketService;
    }

    @Override
    public Game createGame(String name, Player owner, GameConfig gameConfig) throws InterruptedException {
        try {
            owner.getMutex().acquire();

            if (gameManagementService.getGameByName(name) != null) {
                throw new GameAlreadyExistsException();
            }

            if (owner.getCurrentGame() != null) {
                throw new InvalidActionException("Cannot create a game while you are already in one.");
            }

            Game game = new Game();
            game.setName(name);
            owner.setCurrentGame(game);

            List<Player> players = new ArrayList<>();
            players.add(owner);

            game.setOwner(owner);
            game.setPlayers(players);
            game.setGameConfig(gameConfig);

            List<String> deckNames = deckService.getDecksById(gameConfig.getDeckIds())
                    .stream()
                    .map(CardDeck::getDeckName)
                    .collect(Collectors.toList());
            game.getGameConfig().setDeckNames(deckNames);

            gameManagementService.addGame(game);
            gameStateService.setStateLobby(game);
            gameWebsocketService.sendPlayerUpdate(owner);

            return game;
        } finally {
            owner.getMutex().release();
        }
    }

    @Override
    public Game startGame(Game game, Player requestingPlayer) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.

            if (game.getGameState() != GameState.LOBBY) {
                throw new InvalidActionException("Cannot start game when it is not in lobby mode.");
            }

            if (game.getOwner() != requestingPlayer) {
                throw new InvalidActionException("You cannot start the game unless you are the game owner.");
            }

            Set<Phrase> phrases = new HashSet<>();
            Set<Card> cards = new HashSet<>();
            populateCardsAndPhrasesByDeckIds(game.getGameConfig().getDeckIds(), cards, phrases);
            game.setCardSet(cards);
            game.setPhraseSet(phrases);

            Player startingPlayer = CollectionUtil.getRandomItemFromCollection(game.getPlayers());
            Card startingCard = CollectionUtil.getRandomItemFromCollection(game.getCardSet());

            game.setJudgingPlayer(startingPlayer);
            game.setCurrentCard(startingCard);
            manageAllPlayersPhrases(game);
        } finally {
            game.getMutex().release(); // Release lock.
        }

        gameStateService.setStateChoosing(game);
        gameWebsocketService.sendLobbiesUpdate();
        // Note -  here We don't need to send a game update because once the choosing state takes over, one will be sent then.

        return game;
    }

    @Override
    public Game pickPhrasesForCard(Game game, List<String> selectedPhraseIds, Player selectingPlayer) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.
            selectingPlayer.getMutex().acquire();

            logger.info("Attempting to pick phrases: {} by player: {}", selectedPhraseIds, selectingPlayer);

            if (game.getGameState() != GameState.CHOOSING) {
                logger.error("You cannot select phrases when not in choosing mode.");
                throw new WrongGameStateException("You cannot select phrases when not in choosing mode.");
            }

            if (!game.hasPlayer(selectingPlayer)) {
                logger.error("Player is not a member of this game.");
                throw new InvalidActionException("Player is not a member of this game.");
            }

            if (!selectingPlayer.getSelectedPhrases().isEmpty()) {
                logger.error("Player has already selected for this turn.");
                throw new InvalidActionException("Player has already selected a phrase for this turn.");
            }

            if (game.getJudgingPlayer().equals(selectingPlayer)) {
                logger.error("Player cannot select cards because player is a judge for this round.");
                throw new InvalidActionException("Player cannot select cards because player is a judge for this round.");
            }

            if (game.getCurrentCard().getNumPhrases() != selectedPhraseIds.size()) {
                logger.error("Player selected an incorrect amount of phrases for this card.");
                throw new InvalidActionException("Player selected an incorrect amount of phrases for this card.");
            }

            // Make sure they're in the order that they were selected in.
            List<Phrase> phraseSelections = new ArrayList<>(selectedPhraseIds.size());
            for (String phraseUuid : selectedPhraseIds) {
                Phrase selectedPhrase = selectingPlayer.getPhrases().stream()
                        .filter(phrase -> phrase.getUuid().equals(phraseUuid))
                        .findFirst()
                        .orElseThrow(() -> new InvalidActionException("Player does not have one or more of the selected phrases"));
                phraseSelections.add(selectedPhrase);
                selectingPlayer.getSelectedPhrases().add(selectedPhrase);
            }

            game.getPhraseSelections().add(phraseSelections);
            selectingPlayer.getPhrases().removeAll(phraseSelections);

            gameWebsocketService.sendGameUpdate(game);
            gameWebsocketService.sendPlayerUpdate(selectingPlayer);

        } finally {
            selectingPlayer.getMutex().release();
            game.getMutex().release(); // Release lock.
        }

        // If all players have selected, change game state to DONE_CHOOSING.
        if (game.haveAllPlayersSelected()) {
            gameStateService.setStateDoneChoosing(game);
        }

        return game;
    }

    @Override
    public Game vote(Game game, Player votingPlayer, Integer voteIndex) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.

            if (game.getGameState() != GameState.JUDGING) {
                throw new InvalidActionException("Cannot vote when game is not in judging mode.");
            }

            if (!game.isPhraseUpForVote(voteIndex)) {
                throw new InvalidActionException("Phrase was not found, an invalid selection was made.");
            }

            if (!game.getJudgingPlayer().equals(votingPlayer)) {
                throw new InvalidActionException("Player is not the judging player.");
            }

            game.setJudgeChoiceWinner(voteIndex);

            logger.info("Last Winning Player: {}", game.getLastWinningPlayer());
            logger.info("Winning Phrase: {}", game.getPhraseSelections().get(voteIndex));
        } finally {
            game.getMutex().release();
        }

        gameStateService.setStateDoneJudging(game);

        return game;
    }

    @Override
    public Game joinGame(Game game, Player joiningPlayer) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.
            joiningPlayer.getMutex().acquire(); // Lock player for state modifications.

            if (game.isGameFull()) {
                throw new InvalidActionException("Player cannot join because the game is full.");
            }

            if (game.hasPlayer(joiningPlayer)) {
                throw new InvalidActionException("Player has already joined this game.");
            }

            if (joiningPlayer.getCurrentGame() != null) {
                throw new InvalidActionException("Player is already in a game and needs to leave it in order to join.");
            }

            if (game.getGameState() != GameState.LOBBY) {
                // If we're not in the lobby, we need to give this guy some cards.
                managePlayersPhrases(game, joiningPlayer);
            }

            joiningPlayer.setCurrentGame(game);
            joiningPlayer.getSelectedPhrases().clear(); // sanity check - clear these out
            joiningPlayer.setScore(0); // sanity check
            game.getPlayers().add(joiningPlayer);

            gameWebsocketService.sendPlayerUpdate(joiningPlayer);
            gameWebsocketService.sendGameUpdate(game);
            gameWebsocketService.sendLobbiesUpdate();

        } finally {
            joiningPlayer.getMutex().release();
            game.getMutex().release();
        }

        return game;
    }

    @Override
    public Game leaveGame(Game game, Player leavingPlayer) throws InterruptedException {
        boolean isLastPlayerInGame;
        String gameName = game.getName();

        try {
            game.getMutex().acquire(); // Lock game for state modifications.
            leavingPlayer.getMutex().acquire(); // Lock player for state modifications.

            isLastPlayerInGame = game.getPlayers().size() == 1;

            if (!game.hasPlayer(leavingPlayer)) {
                throw new InvalidActionException("Player is not a member of this game.");
            }

            if (!leavingPlayer.getSelectedPhrases().isEmpty() && game.getGameState() == GameState.JUDGING) {
                throw new InvalidActionException("You cannot leave while your cards are being judged.");
            }

            if (!game.getPlayers().remove(leavingPlayer)) {
                throw new InvalidActionException(("We were unable to remove the player from the game."));
            }

            if (!isLastPlayerInGame && game.getOwner().equals(leavingPlayer)) {
                // We need to set a new owner since the current one is leaving.
                Player newOwner = game.getPlayers().get(0); // we've already removed the leaving player.
                game.setOwner(newOwner);
                gameWebsocketService.sendPlayerUpdate(newOwner); // Make sure to send an update to the new owner's
            }

            if (!isLastPlayerInGame &&
                    game.getGameState() == GameState.JUDGING &&
                    game.getJudgingPlayer().equals(leavingPlayer)
            ) {
                // The current judge is leaving while judging - we need to reset
                // TODO: Handle this situation
            }

            leavingPlayer.setCurrentGame(null);
            if (leavingPlayer.getPhrases() != null && game.getPhraseSet() != null) {
                returnPlayersPhrases(game, leavingPlayer);
            }

            if (isLastPlayerInGame) {
                game.setOwner(null);
                game.setGameState(GameState.ABANDONED);
                gameManagementService.removeGame(game.getName());
            }

            leavingPlayer.setScore(0); // sanity check

            gameWebsocketService.sendGameUpdate(game);
            gameWebsocketService.sendPlayerUpdate(leavingPlayer);
            gameWebsocketService.sendLobbiesUpdate();

        } finally {
            leavingPlayer.getMutex().release();
            game.getMutex().release();
        }

        return game;
    }

    @Override
    public Game updateGame(Game game, Player updatingPlayer, GameConfig newGameConfig) throws InterruptedException {
        try {
            game.getMutex().acquire();

            if (game.getGameState() != GameState.LOBBY) {
                throw new WrongGameStateException("Not in the correct game state to update it's configuration. Must be in lobby state.");
            }

            if (!game.getOwner().equals(updatingPlayer)) {
                throw new InvalidActionException("You must be the owning player to change the game settings.");
            }

            // Make sure max players was not changed due to this causing the need to possibly remove players.
            newGameConfig.setMaxPlayers(game.getGameConfig().getMaxPlayers());
            game.setGameConfig(newGameConfig);

            gameWebsocketService.sendLobbiesUpdate();
            gameWebsocketService.sendGameUpdate(game);
        } finally {
            game.getMutex().release();
        }
        return game;
    }

    /**
     * Take the player's phrases back and put them back in the game set for use.
     *
     * @param game Game to return the phrases to.
     * @param player Player returning phrases.
     */
    private void returnPlayersPhrases(Game game, Player player) {
        game.getPhraseSet().addAll(player.getPhrases());
        game.getPhraseSet().addAll(player.getSelectedPhrases());
        player.getPhrases().clear();
        player.getSelectedPhrases().clear();
    }

    private void populateCardsAndPhrasesByDeckIds(List<String> deckIds, Set<Card> cards, Set<Phrase> phrases) {
        List<CardDeck> deckSet = deckService.getDecksById(deckIds);

        for (CardDeck cardDeck : deckSet) {
            phrases.addAll(cardDeck.getPhraseSet());
            cards.addAll(cardDeck.getCardSet());
        }
    }

    // Method assumes game has been mutex'd.
    public void manageAllPlayersPhrases(Game game) {
        game.getPlayers().forEach(player -> {
            managePlayersPhrases(game, player);
            gameWebsocketService.sendPlayerUpdate(player);
        });
    }

    /**
     * Ensure a player has 10 phrases. Clear out any selected phrases.
     *
     * This method assumes the game and player have already been mutexed. BE CAREFUL!
     *
     * @param game Game to pick phrases out of and give to the player.
     * @param player Player to manage the cards of.
     */
    private void managePlayersPhrases(Game game, Player player) {
        logger.info("Managing player {}'s phrases", player);

        if (player.getPhrases().size() >= 10) {
            return;
        }

        if (game.getPhraseSet().isEmpty()) {
            return;
        }

        int numPhrasesToGet = 10 - player.getPhrases().size();
        numPhrasesToGet = Math.min(numPhrasesToGet, game.getPhraseSet().size());
        List<Phrase> list = new ArrayList<>(game.getPhraseSet());
        Collections.shuffle(list);
        Set<Phrase> randomSet = new HashSet<>(list.subList(0, numPhrasesToGet));
        game.getPhraseSet().removeAll(randomSet);
        player.getPhrases().addAll(randomSet);
    }
}
