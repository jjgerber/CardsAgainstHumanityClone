package org.j3y.cards.service;

import org.j3y.cards.exception.GameAlreadyExistsException;
import org.j3y.cards.exception.InvalidActionException;
import org.j3y.cards.exception.WrongGameStateException;
import org.j3y.cards.model.GameConfig;
import org.j3y.cards.model.gameplay.*;
import org.j3y.cards.response.GameSummary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultGameService implements GameService {

    private Map<String, Game> gameMap;

    public DefaultGameService() {
        gameMap = new HashMap<>();
    }

    @Override
    public Game getGameByName(String name) throws InterruptedException {
        Game game = gameMap.get(name);
        checkPhraseWinningState(game); // Check for winning state and any need to continue from it.
        return game;
    }

    @Override
    public Game createGame(String name, Player owner, Set<CardDeck> deckSet, GameConfig gameConfig) {
        // Check if game already exists
        if (gameMap.get(name) != null) {
            throw new GameAlreadyExistsException();
        }

        Game game = new Game();
        game.setName(name);

        // Gather all the phrases and cards.
        Set<Phrase> phrases = new HashSet<>();
        Set<Card> cards = new HashSet<>();
        for (CardDeck cardDeck : deckSet) {
            phrases.addAll(cardDeck.getPhraseSet());
            cards.addAll(cardDeck.getCardSet());
        }

        game.setCardSet(cards);
        game.setPhraseSet(phrases);
        game.setJudgingPlayer(owner);
        game.setPlayerList(Collections.singletonList(owner));
        game.setGameConfig(gameConfig);

        gameMap.put(name, game);

        return game;
    }

    @Override
    public Game startGame(Game game, Player requestingPlayer) throws InterruptedException {
        try {
            game.getMutex().acquire(); // Lock game for state modifications.

            if (game.getGameState() != GameState.LOBBY) {
                throw new InvalidActionException("Cannot start game when it is not in lobby mode.");
            }

            Player startingPlayer = (Player) getRandomItemFromCollection(game.getPlayerList());
            Card startingCard = (Card) getRandomItemFromCollection(game.getCardSet());

            game.setJudgingPlayer(startingPlayer);
            game.setCurrentCard(startingCard);
            manageAllPlayersPhrases(game);
            game.setGameState(GameState.CHOOSING);

        } finally {
            game.getMutex().release(); // Release lock.
        }

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

            // If all players have selected, change game state to voting.
            if (game.haveAllPlayersSelected()) {
                game.setGameState(GameState.VOTING);
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

            if (game.getGameState() != GameState.VOTING) {
                throw new InvalidActionException("Cannot vote when game is not in voting mode.");
            }

            if (game.hasPlayerVoted(votingPlayer)) {
                throw new InvalidActionException("Player has already voted.");
            }

            if (game.isPhraseUpForVote(voteIndex)) {
                throw new InvalidActionException("Phrase was not found, an invalid selection was made.");
            }

            game.getVotes().put(votingPlayer, voteIndex);

            if (game.haveAllPlayersVoted()) {
                List<Player> topVotedPlayers = game.getTopVotesPlayers();
                topVotedPlayers.forEach(game::increasePlayerScore);
            }

        } finally {
            game.getMutex().release(); // Release lock.
        }

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
            game.getPlayerList().add(joiningPlayer);

        } finally {
            joiningPlayer.getMutex().release();
            game.getMutex().release();
        }

        return game;
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
            gsr.setNumPlayers(gameEntry.getValue().getPlayerList().size());
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

    public void checkPhraseWinningState(Game game) throws InterruptedException {
        if (game.getGameState() == GameState.WINNER) {
            if (game.getGameStateUnixTime() < System.currentTimeMillis() - game.getGameConfig().getWinningWaitTimeMillis()) {
                // See if we've reached the max score
                Map.Entry<Player, Integer> topPlayerScore = game.getTopScore();
                if (topPlayerScore.getValue() >= game.getGameConfig().getMaxScore()) {
                    resetPhrases(game);
                    game.setGameState(GameState.LOBBY); // Player has won the game by reaching max score.
                } else {
                    manageAllPlayersPhrases(game);
                    game.setGameState(GameState.CHOOSING); // No winner yet so back to choosing state.
                }
            }
        }
    }

    private void resetPhrases(Game game) throws InterruptedException {
        // Retrieve all phrases back from each player.
        for (Player player : game.getPlayerList()) {
            for (Phrase phrase : player.getPhrases()) {
                player.getMutex().acquire();
                player.getPhrases().remove(phrase);
                game.getPhraseSet().add(phrase);
                player.getMutex().release();
            }
        }

    }

    // Method assumes game has been mutex'd.
    private void manageAllPlayersPhrases(Game game) {
        game.getPlayerList().forEach(p -> managePlayersPhrases(game, p));
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
        if (player.getPhrases().size() >= 10) {
            return;
        }

        int numPhrasesToGet = 10 - player.getPhrases().size();
        Iterator<Phrase> phraseIter = game.getPhraseSet().iterator();
        while (numPhrasesToGet < 10 && phraseIter.hasNext()) {
            Phrase phrase = phraseIter.next();
            game.getPhraseSet().remove(phrase);
            player.getPhrases().add(phrase);
        }
    }

}
