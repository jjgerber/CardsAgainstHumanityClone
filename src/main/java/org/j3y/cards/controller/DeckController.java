package org.j3y.cards.controller;

import org.j3y.cards.model.gameplay.CardDeck;
import org.j3y.cards.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping
public class DeckController {

    DeckService deckService;

    @Autowired
    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping(value = "/v1/deck/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CardDeck getDeck(@PathVariable String uuid) {
        return deckService.getDeckById(uuid);
    }
}
