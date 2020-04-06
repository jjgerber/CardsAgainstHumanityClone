package org.j3y.cards.service;

import org.j3y.cards.exception.GameAlreadyExistsException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.exception.WrongGameStateException;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.gameplay.*;
import org.j3y.cards.response.GameSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultGameService implements GameService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Game> gameMap;
    private final GameStateTimeoutService gameStateTimeoutService;
    private final DeckService deckService;

    @Autowired
    public DefaultGameService(final GameStateTimeoutService gameStateTimeoutService, final DeckService deckService) {
        gameMap = new HashMap<>();
        this.gameStateTimeoutService = gameStateTimeoutService;
        this.deckService = deckService;
    }

    @Override
    public Game getGameByName(String name) {
        return gameMap.get(name);
    }

    @Override
    public Game createGame(String name, Player owner, GameConfig gameConfig) throws InterruptedException {
        // Check if game already exists
        if (gameMap.get(name) != null) {
            throw new GameAlreadyExistsException();
        }

        Game game = new Game();
        game.setName(name);
        owner.setCurrentGame(game);

        List<Player> players = new ArrayList<>();
        players.add(owner);

        game.setOwner(owner);
        game.setPlayers(players);
        game.setGameConfig(gameConfig);

        gameMap.put(name, game);

        setStateLobby(game);

        return game;
    }

    private void populateCardsAndPhrasesByDeckIds(List<String> deckIds, Set<Card> cards, Set<Phrase> phrases) {
        // Gather all the phrases and cards.
        Set<CardDeck> deckSet = deckService.getDecksById(deckIds);

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

            Player startingPlayer = (Player) getRandomItemFromCollection(game.getPlayers());
            Card startingCard = (Card) getRandomItemFromCollection(game.getCardSet());

            game.setJudgingPlayer(startingPlayer);
            game.setCurrentCard(startingCard);
            manageAllPlayersPhrases(game);
        } finally {
            game.getMutex().release(); // Release lock.
        }

        setStateChoosing(game);

        return game;
    }

    @Override
    public Game pickPhrasesForCard(Game game, List<Phrase> selectedPhrases, Player selectingPlayer) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.

            if (game.getGameState() != GameState.CHOOSING) {
                throw new WrongGameStateException("You cannot select phrases when not in choosing mode.");
            }

            if (game.hasPhrases(selectedPhrases)) {
                throw new InvalidActionException("Current game does not have one or more selected phrases available.");
            }

            if (game.hasPlayer(selectingPlayer)) {
                throw new InvalidActionException("Player is not a member of this game.");
            }

            if (game.hasPlayerSelected(selectingPlayer)) {
                throw new InvalidActionException("Player has already selected a phrase for this turn.");
            }

            game.getPhraseSelections().add(selectedPhrases);
            game.getPlayerPhraseSelectionIndexMap().put(selectingPlayer, game.getPhraseSelections().indexOf(selectedPhrases));

            // If all players have selected, change game state to DONE_CHOOSING.
            if (game.haveAllPlayersSelected()) {
                setStateDoneChoosing(game);
            }

        } finally {
            game.getMutex().release(); // Release lock.
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

            if (game.isPhraseUpForVote(voteIndex)) {
                throw new InvalidActionException("Phrase was not found, an invalid selection was made.");
            }

            if (votingPlayer != game.getJudgingPlayer()) {
                throw new InvalidActionException("Player is not the judging player.");
            }

            game.setJudgeChoiceWinner(voteIndex);

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

            joiningPlayer.setCurrentGame(game);
            game.getPlayers().add(joiningPlayer);

        } finally {
            joiningPlayer.getMutex().release();
            game.getMutex().release();
        }

        return game;
    }

    @Override
    public Game leaveGame(Game game, Player leavingPlayer) throws InterruptedException {
        // TODO: Implement this
        return null;
    }

    /* TODO: Move this list into a singleton bean that gets
       TODO: updated on a regular timeschedule by another thread. (Java CRON thingy?) */
    @Override
    public List<GameSummary> getAllGames() {
        List<GameSummary> response = new ArrayList<>();

        for (Map.Entry<String, Game> gameEntry : gameMap.entrySet()) {
            GameSummary gsr = new GameSummary();
            gsr.setGameName(gameEntry.getKey());
            gsr.setGameState(gameEntry.getValue().getGameState().toString());
            gsr.setNumPlayers(gameEntry.getValue().getPlayers().size());
            gsr.setGameConfig(gameEntry.getValue().getGameConfig());

            response.add(gsr);
        }

        return response;
    }

    private Object getRandomItemFromCollection(Collection objects) {
        Random random = new Random();
        int randomIdx = random.nextInt(objects.size());

        int i = 0;
        for (Object object : objects) {
            if (i == randomIdx) {
                return object;
            }
            i++;
        }

        return null; // Should never hit this.
    }

    // Method assumes game has been mutex'd.
    private void manageAllPlayersPhrases(Game game) {
        game.getPlayers().forEach(p -> managePlayersPhrases(game, p));
    }

    /**
     * Ensure a player has 10 phrases.
     *
     * This method assumes the game and player have already been mutexed. BE CAREFUL!
     *
     * @param game Game to pick phrases out of and give to the player.
     * @param player Player to manage the cards of.
     */
    private void managePlayersPhrases(Game game, Player player) {
        Integer curSelectedPhrasesIdx = game.getPlayerPhraseSelectionIndexMap().get(player);
        if (curSelectedPhrasesIdx != null) {
            player.getPhrases().removeAll(game.getPhraseSelections().get(curSelectedPhrasesIdx));
        }

        if (player.getPhrases().size() >= 10) {
            return;
        }

        int numPhrasesToGet = 10 - player.getPhrases().size();
        List<Phrase> list = new ArrayList<>(game.getPhraseSet());
        Collections.shuffle(list);
        Set<Phrase> randomSet = new HashSet<>(list.subList(0, numPhrasesToGet));
        game.getPhraseSet().removeAll(randomSet);
        player.setPhrases(randomSet);
    }

    @Override
    public void setStateChoosing(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();
            game.setGameState(GameState.CHOOSING);

            // Sort out the judge
            Player lastJudgingPlayer = game.getJudgingPlayer();
            int nextPlayerIdx = (game.getPlayers().indexOf(lastJudgingPlayer) + 1) % game.getPlayers().size();
            Player nextJudgingPlayer = game.getPlayers().get(nextPlayerIdx);
            game.setJudgingPlayer(nextJudgingPlayer);
            game.setCurrentCard((Card) getRandomItemFromCollection(game.getCardSet()));
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
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.JUDGING);
        } finally {
            game.getMutex().release();
        }

        gameStateTimeoutService.setJudgingTimeout(game);
    }

    @Override
    public void setStateDoneJudging(Game game) throws InterruptedException {
        try {
            game.getMutex().acquire();

            game.setGameState(GameState.DONE_JUDGING);

            Player winner = game.getRoundWinner();
            if (winner == null) {
                // TODO: Give all phrases back to original owners
            } else {
                manageAllPlayersPhrases(game);
                winner.incrementScore();
                game.setLastWinningPlayer(winner);
            }

        } finally {
            game.getMutex().release();
        }

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
        } finally {
            game.getMutex().release();
        }
    }
}
