package org.j3y.cards.service;

import org.j3y.cards.model.CardDeck;

import java.util.List;

public interface DeckService {
    CardDeck getDeckById(String uuid);
    List<CardDeck> getDecksById(Iterable<String> uuids);
    List<CardDeck> getAllDecks();
}
