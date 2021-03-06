package org.j3y.cards.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.j3y.cards.model.ChatMessage;
import org.j3y.cards.model.Game;
import org.j3y.cards.model.Player;
import org.j3y.cards.model.Views;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DefaultGameWebsocketService implements GameWebsocketService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DefaultGameManagementService gameManagementService;
    private final SimpMessagingTemplate simpTemplate;
    private final ObjectMapper mapper;

    @Autowired
    public DefaultGameWebsocketService(
            final DefaultGameManagementService gameManagementService,
            final SimpMessagingTemplate simpTemplate,
            final ObjectMapper mapper
    ) {
        this.gameManagementService = gameManagementService;
        this.simpTemplate = simpTemplate;
        this.mapper = mapper;
    }


    @Override
    public void sendPlayerUpdate(Player player) {
        String playerJson = "{}";
        try {
            playerJson = mapper.writerWithView(Views.LoggedInUser.class).writeValueAsString(player);
        } catch (JsonProcessingException e) {
            logger.error("Error converting player to JSON: {}", e.getMessage(), e);
        }
        logger.info("Sending Player Update: {}", playerJson);
        simpTemplate.convertAndSendToUser(player.getName(), "/userInfo", playerJson);
    }

    @Override
    public void sendGameUpdate(Game game) {
        String gameJson = gameManagementService.getGameJson(game);

        logger.info("Sending Game Update: {}", gameJson);
        this.simpTemplate.convertAndSend("/topic/game/" + game.getName(), gameJson);
    }

    @Override
    public void sendGameChatMessage(Game game, String message) {
        ChatMessage chatMessage = new ChatMessage("SERVER","Game", message );
        this.simpTemplate.convertAndSend("/topic/chat/" + game.getName(), chatMessage);
    }

    @Override
    public void sendLobbiesUpdate() {
        String gamesJson = "[]";
        try {
            gamesJson = mapper.writerWithView(Views.Limited.class).writeValueAsString(gameManagementService.getAllGames());
        } catch (JsonProcessingException e) {
            logger.error("Error converting lobby listing to JSON: {}", e.getMessage(), e);
        }
        logger.info("Sending Lobby Update: {}", gamesJson);
        this.simpTemplate.convertAndSend("/topic/lobbies", gamesJson);
    }


}
