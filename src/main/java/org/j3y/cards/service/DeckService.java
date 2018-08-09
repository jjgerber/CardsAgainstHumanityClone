package org.j3y.cards.service;

import org.j3y.cards.model.gameplay.CardDeck;

import java.util.Set;

public interface DeckService {
    CardDeck getDeckById(String uuid);
    Set<CardDeck> getDecksById(Iterable<String> uuids);
}
