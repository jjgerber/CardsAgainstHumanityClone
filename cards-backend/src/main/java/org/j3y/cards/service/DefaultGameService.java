package org.j3y.cards.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.j3y.cards.exception.GameAlreadyExistsException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.exception.WrongGameStateException;
import org.j3y.cards.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultGameService implements GameService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Game> gameMap;
    private final GameStateTimeoutService gameStateTimeoutService;
    private final DeckService deckService;
    private final SimpMessagingTemplate simpTemplate;
    private final ObjectMapper mapper;

    @Autowired
    public DefaultGameService(
            final GameStateTimeoutService gameStateTimeoutService,
            final DeckService deckService,
            final SimpMessagingTemplate simpTemplate,
            final ObjectMapper mapper
    ) {
        gameMap = new HashMap<>();
        this.gameStateTimeoutService = gameStateTimeoutService;
        this.deckService = deckService;
        this.simpTemplate = simpTemplate;
        this.mapper = mapper;
    }

    @Override
    public Game getGameByName(String name) {
        return gameMap.get(name);
    }

    @Override
    public Game createGame(String name, Player owner, GameConfig gameConfig) throws InterruptedException {
        try {
            owner.getMutex().acquire();
            if (gameMap.containsKey(name)) {
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

            gameMap.put(name, game);

            setStateLobby(game);
            sendPlayerUpdate(owner);

            return game;
        } finally {
            owner.getMutex().release();
        }
    }

    private void sendPlayerUpdate(Player player) {
        String playerJson = "{}";
        try {
            playerJson = mapper.writerWithView(Views.LoggedInUser.class).writeValueAsString(player);
        } catch (JsonProcessingException e) {
            logger.error("Error converting player to JSON: {}", e.getMessage(), e);
        }
        logger.info("Sending Player Update: {}", playerJson);
        simpTemplate.convertAndSendToUser(player.getName(), "/userInfo", playerJson);
    }

    public void sendGameUpdate(Game game) {
        sendGameUpdate(game, Views.Full.class);
    }

    public void sendGameUpdate(Game game, Class view) {
        String gameJson = "{}";

        if (game != null) {
            try {
                gameJson = mapper.writerWithView(view).writeValueAsString(game);
            } catch (JsonProcessingException e) {
                logger.error("Error converting game to JSON: {}", e.getMessage(), e);
            }
        }

        logger.info("Sending Game Update: {}", gameJson);
        this.simpTemplate.convertAndSend("/topic/game/" + game.getName(), gameJson);
    }

    private void sendLobbiesUpdate() {
        String gamesJson = "[]";
        try {
            List<Game> games = getAllGames();
            gamesJson = mapper.writerWithView(Views.Limited.class).writeValueAsString(games);
        } catch (JsonProcessingException e) {
            logger.error("Error converting lobby listing to JSON: {}", e.getMessage(), e);
        }
        logger.info("Sending Lobby Update: {}", gamesJson);
        this.simpTemplate.convertAndSend("/topic/lobbies", gamesJson);
    }

    private void populateCardsAndPhrasesByDeckIds(List<String> deckIds, Set<Card> cards, Set<Phrase> phrases) {
        List<CardDeck> deckSet = deckService.getDecksById(deckIds);

        for (CardDeck cardDeck : deckSet) {
            phrases.addAll(cardDeck.getPhraseSet());
            cards.addAll(cardDeck.getCardSet());
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

            Player startingPlayer = getRandomItemFromCollection(game.getPlayers());
            Card startingCard = getRandomItemFromCollection(game.getCardSet());

            game.setJudgingPlayer(startingPlayer);
            game.setCurrentCard(startingCard);
            manageAllPlayersPhrases(game);
        } finally {
            game.getMutex().release(); // Release lock.
        }

        setStateChoosing(game);
        sendLobbiesUpdate();
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

            sendGameUpdate(game);
            sendPlayerUpdate(selectingPlayer);

        } finally {
            selectingPlayer.getMutex().release();
            game.getMutex().release(); // Release lock.
        }

        // If all players have selected, change game state to DONE_CHOOSING.
        if (game.haveAllPlayersSelected()) {
            setStateDoneChoosing(game);
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

        setStateDoneJudging(game);

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

            sendPlayerUpdate(joiningPlayer);
            sendGameUpdate(game);
            sendLobbiesUpdate();

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
                sendPlayerUpdate(newOwner); // Make sure to send an update to the new owner's
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

                // take selected phrases back - we have confirmed that they haven't been judged yet above.
                game.getPhraseSet().addAll(leavingPlayer.getSelectedPhrases());
                leavingPlayer.getSelectedPhrases().clear(); // sanity check - clear these out
            }

            if (isLastPlayerInGame) {
                this.gameMap.remove(gameName);
                game.setOwner(null);
                game.setGameState(GameState.ABANDONED);
            }


            leavingPlayer.setScore(0); // sanity check

            sendGameUpdate(game);
            sendPlayerUpdate(leavingPlayer);
            sendLobbiesUpdate();

        } finally {
            leavingPlayer.getMutex().release();
            game.getMutex().release();
        }

        return game;
    }

    @Override
    public List<Game> getAllGames() {
        return new ArrayList<>(gameMap.values());
    }

    private <T> T getRandomItemFromCollection(Collection<T> objects) {
        Random random = new Random();
        int randomIdx = random.nextInt(objects.size());

        int i = 0;
        for (T object : objects) {
            if (i == randomIdx) {
                return object;
            }
            i++;
        }

        return null; // Should never hit this.
    }

    // Method assumes game has been mutex'd.
    private void manageAllPlayersPhrases(Game game) {
        game.getPlayers().forEach(player -> {
            managePlayersPhrases(game, player);
            sendPlayerUpdate(player);
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
        Set<Phrase> randomSet = new HashSet<>(list.subList(0, numPhrasesToGet));
        game.getPhraseSet().removeAll(randomSet);
        player.getPhrases().addAll(randomSet);
    }

    @Override
    public void setStateChoosing(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();
            game.setGameState(GameState.CHOOSING);

            // Clear out the phrase selections
            game.getPhraseSelections().clear();
            game.setJudgeChoiceWinner(null);

            // Sort out the judge
            Player lastJudgingPlayer = game.getJudgingPlayer();
            int nextPlayerIdx = (game.getPlayers().indexOf(lastJudgingPlayer) + 1) % game.getPlayers().size();
            Player nextJudgingPlayer = game.getPlayers().get(nextPlayerIdx);
            game.setJudgingPlayer(nextJudgingPlayer);
            game.setCurrentCard(getRandomItemFromCollection(game.getCardSet()));
        } finally {
            game.getMutex().release();
        }

        gameStateTimeoutService.setChoosingTimeout(game);
    }

    @Override
    public void setStateDoneChoosing(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();
            game.setGameState(GameState.DONE_CHOOSING);
        } finally {
            game.getMutex().release();
        }

        setStateJudging(game);
    }

    private void setStateJudging(Game game) throws InterruptedException {

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
    public void setStateDoneJudging(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.DONE_JUDGING);
            boolean returnPhrases = false;

            Player winner = game.getRoundWinner();
            if (winner == null) {
                returnPhrases = true;
            } else {
                manageAllPlayersPhrases(game);
                winner.incrementScore();
                game.setLastWinningPlayer(winner);
            }

            for (Player player : game.getPlayers()) {
                try {
                    player.getMutex().acquire();

                    List<Phrase> selectedPhrases = player.getSelectedPhrases();
                    if (selectedPhrases.isEmpty()) continue;

                    if (returnPhrases) {
                        player.getPhrases().addAll(player.getSelectedPhrases());
                    }

                    player.getSelectedPhrases().clear();
                    sendPlayerUpdate(player);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while trying to give player back their cards.");
                } finally {
                    player.getMutex().release();
                }
            }
        } finally {
            game.getMutex().release();
        }

        // Game update will be sent to clients by the gameStateTimeoutService w/ timeout time.
        gameStateTimeoutService.setDoneJudgingTimeout(game);
    }

    @Override
    public void setStateGameOver(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.GAME_OVER);
            game.setJudgingPlayer(null);
            game.setLastWinningPlayer(null);
            game.setJudgeChoiceWinner(null);
            game.setCurrentCard(null);
        } finally {
            game.getMutex().release();
        }

        // Game update will be sent to clients by the gameStateTimeoutService w/ timeout time.
        gameStateTimeoutService.setGameOverTimeout(game);
    }

    @Override
    public void setStateLobby(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.LOBBY);
            game.setCardSet(null);
            game.setPhraseSet(null);
            game.setCurrentCard(null);
            game.setJudgingPlayer(null);
            game.setJudgeChoiceWinner(null);
            game.setLastWinningPlayer(null);
            game.getPlayers().forEach(player -> player.setScore(0));

            sendLobbiesUpdate();
            sendGameUpdate(game);
        } finally {
            game.getMutex().release();
        }

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

            sendLobbiesUpdate();
            sendGameUpdate(game);
        } finally {
            game.getMutex().release();
        }
        return game;
    }

    @Scheduled(fixedDelay = 60000)
    protected void cleanLobbies() {
        ZonedDateTime oldLobbyCuttoffTime = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(30);
        List<Game> games = gameMap.values()
                .stream()
                .filter(game -> game.getGameStateTime().isBefore(oldLobbyCuttoffTime))
                .collect(Collectors.toList());
        if (!games.isEmpty()) {
            logger.info("Purging these games from memory for lack of activity: {}", games);
            games.stream().map(Game::getPlayers).forEach(players -> players.forEach(player -> player.setCurrentGame(null)));
            games.stream().map(Game::getName).forEach(gameMap::remove);
        }
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
    }
}
