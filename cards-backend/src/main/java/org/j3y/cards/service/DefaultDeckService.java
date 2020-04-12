package org.j3y.cards.service;

import org.j3y.cards.model.gameplay.CardDeck;
import org.j3y.cards.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DefaultDeckService implements DeckService {

    private DeckRepository deckRepository;

    @Autowired
    public DefaultDeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Override
    public CardDeck getDeckById(String uuid) {
        Optional<CardDeck> deck = deckRepository.findById(uuid);

        return deck.orElse(null);
    }

    @Override
    public List<CardDeck> getDecksById(Iterable<String> uuids) {
        return deckRepository.findAllById(uuids);
    }

    @Override
    public List<CardDeck> getAllDecks() {
        return deckRepository.findAll();
    }
}