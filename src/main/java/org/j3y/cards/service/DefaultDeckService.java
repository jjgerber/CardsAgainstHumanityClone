package org.j3y.cards.service;

import org.j3y.cards.model.gameplay.CardDeck;
import org.j3y.cards.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    public Set<CardDeck> getDecksById(Iterable<String> uuids) {
        Iterable<CardDeck> cardDecks = deckRepository.findAllById(uuids);
        return new HashSet<>((Collection<CardDeck>) cardDecks);
    }
}
