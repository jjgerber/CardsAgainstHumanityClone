package org.j3y.cards.controller;

import org.apache.commons.text.StringEscapeUtils;
import org.j3y.cards.model.ChatMessage;
import org.j3y.cards.model.gameplay.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @MessageMapping("/chat/{gameName}")
    public ChatMessage receiveMessage(@DestinationVariable String gameName, String message, SimpMessageHeaderAccessor headerAccessor) {
        Player player = (Player) headerAccessor.getSessionAttributes().get("player");
        ChatMessage chatMessage = new ChatMessage(player.getName(), player.getPlayerName(), StringEscapeUtils.escapeHtml4(message));
        logger.info("#{} - {}: {}", gameName, chatMessage.getPlayerName(), chatMessage.getMessage());
        return chatMessage;
    }

}
